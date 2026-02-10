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
        // 1. 初始化上下文
        AnalysisContext ctx = initContext(taskId);
        
        // 2. 获取 Workflow 配置
        List<AiWorkflowStepDTO> steps = getWorkflowSteps(ctx.getWorkflowId());
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("未找到有效的生成方案配置: workflowId=" + ctx.getWorkflowId());
        }

        // 3. 初始化导航器
        WorkflowNavigator navigator = new WorkflowNavigator(steps);

        // 4. 循环执行步骤
        while (navigator.hasNext()) {
            AiWorkflowStepDTO step = navigator.next();
            
            // 执行步骤
            String aiResult = executeStep(step, ctx, navigator.totalSteps(), navigator.currentStepIndex());
            
            // 自动反馈闭环 (Auto-Feedback Loop)
            // 如果是最后一步(通常是审计)，且包含失败标记
            if (navigator.isLastStep() && aiResult.contains(AUDIT_FAIL_FLAG)) {
                handleRetry(navigator, ctx, step);
            }
        }

        // 5. 最终结果处理
        String lastCode = ctx.getLastCode();
        if (StrUtil.isBlank(lastCode)) {
             // 兜底：如果全程没生成代码，尝试取最后一次响应
             lastCode = cleanAiResult(ctx.getLastResponse());
        }
        
        // 6. 存入 Redis
        String codeKey = RedisConstants.getTaskCodeKey(taskId);
        redisTemplate.opsForValue().set(codeKey, lastCode, 1, TimeUnit.HOURS);
        log.info("多 Agent 协同生成完成: taskId={}", taskId);

        return lastCode;
    }

    private AnalysisContext initContext(Long taskId) {
        String dataKey = RedisConstants.getTaskDataKey(taskId);
        String jsonData = redisTemplate.opsForValue().get(dataKey);
        if (jsonData == null) {
            throw new RuntimeException("任务数据不存在或已过期: " + taskId);
        }
        TaskSubmitDTO taskData = JSONUtil.toBean(jsonData, TaskSubmitDTO.class);
        return new AnalysisContext(taskId, taskData);
    }

    private String executeStep(AiWorkflowStepDTO step, AnalysisContext ctx, int totalSteps, int currentStepIndex) {
        // 更新状态进度
        updateProgress(ctx.getTaskId(), currentStepIndex, totalSteps, step.getRoleName(), ctx.getRetryCount());
        
        log.info("执行步骤 [{}]: {} - taskId={} (retry={})", step.getStepOrder(), step.getRoleName(), ctx.getTaskId(), ctx.getRetryCount());
        
        // 1. 渲染 Prompt
        String sysPrompt = step.getSystemPrompt();
        String userPrompt = StrUtil.format(step.getUserPromptTemplate(), ctx.getVars());
        
        // 自动注入审计意见
        if (ctx.hasAuditAdvice()) {
            userPrompt += "\n\n【系统修正指令】\n上一步代码审计未通过，请基于以下反馈进行修正：\n" + ctx.getAuditAdvice();
            ctx.clearAuditAdvice();
        }

        // 2. 调用 AI
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

        // 3. 记录日志 (异步发送 MQ)
        sendExecutionLog(ctx.getTaskId(), step, userPrompt, aiResult);

        // 4. 智能分流与变量注册
        parseAndRegisterContext(ctx.getVars(), step.getStepOrder(), aiResult);
        
        // 5. 发送实时预览
        String textPreview = (String) ctx.getVar("step_" + step.getStepOrder() + "_text");
        if (StrUtil.isNotBlank(textPreview)) {
            updateProgressWithPreview(ctx.getTaskId(), currentStepIndex, totalSteps, textPreview);
        }
        
        return aiResult;
    }

    private void handleRetry(WorkflowNavigator navigator, AnalysisContext ctx, AiWorkflowStepDTO auditStep) {
        // 1. 校验与更新重试次数
        if (!ctx.canRetry()) {
            throw new RuntimeException("AI 生成重试次数耗尽 (" + ctx.getRetryCount() + "/" + MAX_RETRY_COUNT + ")");
        }
        ctx.incrementRetry();

        // 2. 提取并保存审计意见
        String advice = ctx.getLastResponse();
        if (StrUtil.isBlank(advice)) {
            advice = "审计未通过，请重新检查代码。"; 
        }
        ctx.setAuditAdvice(advice);
        
        log.warn("审计未通过，触发重试 {}/{}. 意见: {}", ctx.getRetryCount(), MAX_RETRY_COUNT, advice);

        // 3. 执行回滚
        boolean success = navigator.rollbackToLastCodeProducer(ctx);
        
        if (!success) {
            throw new RuntimeException("审计未通过且无法回滚：未找到前序生成步骤（无代码产出记录）。");
        }
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
            context.put("latest_code", extractedCode);
        }
        
        context.put("step_" + stepOrder + "_text", extractedText);
        
        // 记录最后一次原始响应 (兼容旧逻辑)
        context.put("last_response", aiResult);
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
    
    // Inner Classes

    private static class AnalysisContext {
        private final Long taskId;
        private final TaskSubmitDTO taskData;
        private final Map<String, Object> vars = new HashMap<>();
        private int retryCount = 0;
        private String auditAdvice = null;

        public AnalysisContext(Long taskId, TaskSubmitDTO taskData) {
            this.taskId = taskId;
            this.taskData = taskData;
            initVars();
        }
        
        private void initVars() {
            vars.put("title", taskData.getTitle());
            vars.put("description", taskData.getProblemDescription());
            vars.put("standardCode", taskData.getStandardCode());
            vars.put("testcaseCount", taskData.getTestcaseCount());
            vars.put("timeLimit", taskData.getTimeLimit());
            vars.put("memoryLimit", taskData.getMemoryLimit());
        }

        public Long getTaskId() { return taskId; }
        public Long getWorkflowId() { return taskData.getWorkflowId(); }
        public Map<String, Object> getVars() { return vars; }
        public int getRetryCount() { return retryCount; }
        public void incrementRetry() { this.retryCount++; }
        public boolean canRetry() { return retryCount < MAX_RETRY_COUNT; }
        
        public void setAuditAdvice(String advice) { this.auditAdvice = advice; }
        public String getAuditAdvice() { return auditAdvice; }
        public boolean hasAuditAdvice() { return auditAdvice != null; }
        public void clearAuditAdvice() { this.auditAdvice = null; }

        public Object getVar(String key) { return vars.get(key); }
        
        public String getStepText(Integer stepOrder) {
            return (String) vars.get("step_" + stepOrder + "_text");
        }
        
        public boolean hasCodeOutput(Integer stepOrder) {
            String key = "step_" + stepOrder + "_code";
            return vars.containsKey(key) && StrUtil.isNotBlank((String) vars.get(key));
        }

        public String getLastCode() {
            return (String) vars.get("latest_code");
        }
        
        public String getLastResponse() {
            return (String) vars.get("last_response");
        }
    }

    private static class WorkflowNavigator {
        private final List<AiWorkflowStepDTO> steps;
        private int cursor = -1;

        public WorkflowNavigator(List<AiWorkflowStepDTO> steps) {
            this.steps = steps;
        }

        public boolean hasNext() {
            return cursor < steps.size() - 1;
        }

        public AiWorkflowStepDTO next() {
            cursor++;
            return steps.get(cursor);
        }
        
        public int currentStepIndex() { return cursor; }
        public int totalSteps() { return steps.size(); }
        
        public boolean isLastStep() {
            return cursor == steps.size() - 1;
        }

        public boolean rollbackToLastCodeProducer(AnalysisContext ctx) {
            for (int i = cursor - 1; i >= 0; i--) {
                AiWorkflowStepDTO step = steps.get(i);
                if (ctx.hasCodeOutput(step.getStepOrder())) {
                    cursor = i - 1; 
                    return true;
                }
            }
            return false;
        }
    }
}
