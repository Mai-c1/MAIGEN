package com.maigen.analysis.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.maigen.common.core.enums.TaskStatusEnum;
import com.maigen.common.core.model.dto.AiWorkflowStepDTO;
import com.maigen.common.core.model.dto.TaskExecutionLogDTO;
import com.maigen.common.core.model.dto.TaskStatusDTO;
import com.maigen.common.core.model.dto.TaskSubmitDTO;
import com.maigen.common.rabbitmq.constant.RabbitMQConstants;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiAnalysisService {

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    // 智能分流正则：提取 markdown 代码块内容
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("(?s)```(?:\\w+)?\\n(.*?)\\n```");
    
    // 审计失败标记
    private static final String AUDIT_FAIL_FLAG = "[FAIL]";
    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    public AiAnalysisService(ChatClient.Builder chatClientBuilder, 
                             StringRedisTemplate redisTemplate,
                             RabbitTemplate rabbitTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 执行 AI 分析并生成代码 (多 Agent 协同版 + 智能分流 + 自动闭环)
     */
    public String analyzeAndGenerate(Long taskId) {
        // 1. 获取任务数据
        String dataKey = RedisConstants.getTaskDataKey(taskId);
        String jsonData = redisTemplate.opsForValue().get(dataKey);
        if (jsonData == null) {
            throw new RuntimeException("任务数据不存在或已过期: " + taskId);
        }
        TaskSubmitDTO taskData = JSONUtil.toBean(jsonData, TaskSubmitDTO.class);
        Long workflowId = taskData.getWorkflowId();

        // 2. 获取 Workflow 配置
        List<AiWorkflowStepDTO> steps = getWorkflowSteps(workflowId);
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("未找到有效的生成方案配置: workflowId=" + workflowId);
        }

        // 3. 初始化上下文
        Map<String, Object> context = new HashMap<>();
        context.put("title", taskData.getTitle());
        context.put("description", taskData.getProblemDescription());
        context.put("standardCode", taskData.getStandardCode());
        context.put("testcaseCount", taskData.getTestcaseCount());
        context.put("timeLimit", taskData.getTimeLimit());
        context.put("memoryLimit", taskData.getMemoryLimit());
        
        String lastCode = "";
        int retryCount = 0;
        String auditAdvice = null;

        // 4. 循环执行步骤 (支持重试回滚)
        for (int i = 0; i < steps.size(); i++) {
            AiWorkflowStepDTO step = steps.get(i);
            
            // 更新状态进度
            updateProgress(taskId, i, steps.size(), step.getRoleName(), retryCount);
            
            log.info("执行步骤 [{}]: {} - taskId={} (retry={})", step.getStepOrder(), step.getRoleName(), taskId, retryCount);
            
            // 4.1 渲染 Prompt
            String sysPrompt = step.getSystemPrompt();
            String userPrompt = StrUtil.format(step.getUserPromptTemplate(), context);
            
            // 自动注入审计意见 (Auto-Injection)
            if (auditAdvice != null && isGenerationStep(step)) {
                userPrompt += "\n\n【系统修正指令】\n上一步代码审计未通过，请基于以下反馈进行修正：\n" + auditAdvice;
                // 注入后清除意见，避免污染后续
                auditAdvice = null; 
            }

            // 4.2 调用 AI
            String aiResult;
            try {
                aiResult = chatClient.prompt()
                        .system(sysPrompt)
                        .user(userPrompt)
                        .call()
                        .content();
            } catch (Exception e) {
                log.error("AI 调用失败: step={}, role={}", step.getStepOrder(), step.getRoleName(), e);
                throw e; 
            }

            // 4.3 记录日志 (异步发送 MQ)
            sendExecutionLog(taskId, step, userPrompt, aiResult);

            // 4.4 智能分流与变量注册 (Smart Parsing)
            parseAndRegisterContext(context, step.getStepOrder(), aiResult);
            
            // 4.4.1 发送实时预览 (Step 完成反馈)
            String textPreview = (String) context.get("step_" + step.getStepOrder() + "_text");
            if (StrUtil.isNotBlank(textPreview)) {
                updateProgressWithPreview(taskId, i, steps.size(), textPreview);
            }
            
            // 4.5 自动反馈闭环 (Auto-Feedback Loop)
            // 如果是最后一步(通常是审计)，且包含失败标记
            if (i == steps.size() - 1 && aiResult.contains(AUDIT_FAIL_FLAG)) {
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++;
                    // 提取审计意见 (即这一步的纯文本部分)
                    auditAdvice = (String) context.get("step_" + step.getStepOrder() + "_text");
                    log.warn("审计未通过，触发重试 {}/{}. 意见: {}", retryCount, MAX_RETRY_COUNT, auditAdvice);
                    
                    // 回滚指针到生成步 (假设生成步是倒数第二步，即 index - 1)
                    // 更稳健的做法是回滚到上一个 "生成类" 步骤，这里简单处理回退一步
                    if (i > 0) {
                        i -= 2; // 下一轮循环 i++ 后变成 i-1
                        continue;
                    }
                } else {
                    throw new RuntimeException("AI 生成重试次数耗尽，审计未通过: " + aiResult);
                }
            }
            
            // 更新最后生成的代码
            if (context.containsKey("step_" + step.getStepOrder() + "_code")) {
                lastCode = (String) context.get("step_" + step.getStepOrder() + "_code");
            }
        }

        // 5. 最终结果处理
        if (StrUtil.isBlank(lastCode)) {
             // 兜底：如果全程没生成代码，尝试取最后一步的 raw result
             lastCode = cleanAiResult((String) context.get("last_response"));
        }
        
        // 6. 存入 Redis
        String codeKey = RedisConstants.getTaskCodeKey(taskId);
        redisTemplate.opsForValue().set(codeKey, lastCode, 1, TimeUnit.HOURS);
        log.info("多 Agent 协同生成完成: taskId={}", taskId);

        return lastCode;
    }
    
    /**
     * 智能解析并注册上下文
     */
    private void parseAndRegisterContext(Map<String, Object> context, Integer stepOrder, String aiResult) {
        // 1. 提取代码块
        StringBuilder codeBuilder = new StringBuilder();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(aiResult);
        while (matcher.find()) {
            codeBuilder.append(matcher.group(1)).append("\n");
        }
        String extractedCode = codeBuilder.toString().trim();
        
        // 2. 提取纯文本 (移除代码块后的剩余内容)
        String extractedText = matcher.replaceAll("").trim();
        
        // 3. 注册变量
        if (StrUtil.isNotBlank(extractedCode)) {
            context.put("step_" + stepOrder + "_code", extractedCode);
        }
        
        context.put("step_" + stepOrder + "_text", extractedText);
        
        // 记录最后一次原始响应 (兼容旧逻辑)
        context.put("last_response", aiResult);
    }
    
    private boolean isGenerationStep(AiWorkflowStepDTO step) {
        // 简单启发式判断：如果角色名包含 "开发" 或 "生成" 或 "Developer"
        // 实际项目中可以在数据库加个 type 字段
        String role = step.getRoleName();
        return role != null && (role.contains("开发") || role.contains("生成") || role.contains("Developer") || role.contains("Coder"));
    }

    private void updateProgress(Long taskId, int currentStepIndex, int totalSteps, String roleName, int retryCount) {
        // 映射进度：20% -> 60%
        int baseProgress = 20;
        int maxProgress = 60;
        int progress = baseProgress + (int)((double)(currentStepIndex + 1) / totalSteps * (maxProgress - baseProgress));
        
        TaskStatusEnum status = TaskStatusEnum.GENERATING;
        if (progress < 20) status = TaskStatusEnum.ANALYZING;
        
        String msg = StrUtil.format("执行步骤 [{}]: {}", currentStepIndex + 1, roleName);
        if (retryCount > 0) {
            msg += StrUtil.format(" (重试 {})", retryCount);
        }
        
        TaskStatusDTO statusDTO = TaskStatusDTO.builder()
                .taskId(taskId)
                .status(status.getCode())
                .progress(progress)
                .message(msg)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, 
                RabbitMQConstants.QUEUE_TASK_STATUS, statusDTO);
    }
    
    /**
     * 更新带有预览内容的进度 (Step 完成后调用)
     */
    private void updateProgressWithPreview(Long taskId, int currentStepIndex, int totalSteps, String textPreview) {
        // 映射进度：同上，但可以稍微增加一点点表示该步完成
        int baseProgress = 20;
        int maxProgress = 60;
        int progress = baseProgress + (int)((double)(currentStepIndex + 1) / totalSteps * (maxProgress - baseProgress));
        
        // 截取前 30 个字符
        String preview = StrUtil.subPre(textPreview.replace("\n", " "), 30);
        if (textPreview.length() > 30) preview += "...";
        
        TaskStatusDTO statusDTO = TaskStatusDTO.builder()
                .taskId(taskId)
                .status(TaskStatusEnum.GENERATING.getCode())
                .progress(progress)
                .message("步骤 " + (currentStepIndex + 1) + " 完成: " + preview)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE_TASK, 
                RabbitMQConstants.QUEUE_TASK_STATUS, statusDTO);
    }

    private List<AiWorkflowStepDTO> getWorkflowSteps(Long workflowId) {
        if (workflowId == null) return null;
        Object obj = redisTemplate.opsForHash().get(RedisConstants.SYS_AI_WORKFLOWS_KEY, workflowId.toString());
        if (obj != null) {
            return JSONUtil.toList(obj.toString(), AiWorkflowStepDTO.class);
        }
        return null;
    }

    private void sendExecutionLog(Long taskId, AiWorkflowStepDTO step, String prompt, String response) {
        TaskExecutionLogDTO logDTO = TaskExecutionLogDTO.builder()
                .taskId(taskId)
                .stepOrder(step.getStepOrder())
                .roleName(step.getRoleName())
                .promptSnapshot(prompt)
                .aiResponse(response)
                .createTime(LocalDateTime.now())
                .build();
        
        rabbitTemplate.convertAndSend(RabbitMQConstants.QUEUE_TASK_LOG, logDTO);
    }

    private String cleanAiResult(String result) {
        if (result == null) return "";
        result = result.trim();
        if (result.startsWith("```json")) {
            result = result.substring(7);
        } else if (result.startsWith("```python")) {
            result = result.substring(9);
        } else if (result.startsWith("```java")) {
            result = result.substring(7);
        } else if (result.startsWith("```")) {
            result = result.substring(3);
        }
        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3);
        }
        return result.trim();
    }
}
