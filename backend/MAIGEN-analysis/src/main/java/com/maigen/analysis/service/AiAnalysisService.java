package com.maigen.analysis.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.maigen.common.core.model.dto.TaskSubmitDTO;
import com.maigen.common.redis.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AiAnalysisService {

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    public AiAnalysisService(ChatClient.Builder chatClientBuilder, StringRedisTemplate redisTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.redisTemplate = redisTemplate;
    }

    /**
     * 系统提示词模板
     */
    private static final String SYSTEM_PROMPT = """
            你是一个资深的算法竞赛命题专家。
            你的任务是根据用户提供的题目描述，编写基于 Python Cyaron 库的数据生成脚本。
            
            【核心库规范 (参考 Cyaron Wiki)】
            1. 基础结构：
               import os
               from cyaron import *
               if not os.path.exists('./data/'): os.makedirs('./data/')
            2. 文件命名：
               使用 `io = IO(file_prefix="", test_data_number=i, path='./data/')`\s
               生成 ./data/1.in, ./data/2.in 等。
            3. 核心 API 推荐：
               - 图论：Graph.graph(n, m, weight_limit=(l, r), self_loop=False)
               - 树：Graph.tree(n, chain=0.3, spider=0.2)
               - 序列/数组：Vector.random(size, [(min, max)])
               - 随机数/字符：randint(l, r), String.random(len, charset)
            
            【编写准则 - 解决幻觉问题】
            1. **动态识别规模**：请仔细分析题目中“数据范围”部分提到的核心变量（可能是 n, m, k, len, t 等）。不要生搬硬套 N 或 M，如果题目是字符串题，请关注长度约束；如果题目是多组询问，请关注询问次数。
            2. **阶梯式构造**：生成的 N 组数据应包含从“最小约束（如 n=1）”到“最大满额约束”的平滑过渡。利用循环变量 `i` 动态计算当前测试点的规模。
            3. **合法性检查**：确保生成的逻辑符合题目逻辑（如：生成树时节点数必须大于0；生成不重复序列时范围必须足够）。
            
            【约束要求】
            1. 仅生成输入文件 (.in)，无需生成输出文件。
            2. 返回格式必须是纯 JSON 字符串，禁止包含 Markdown 标签。
            
            【返回 JSON 结构】
            {
              "code": "Python 代码字符串",
              "explanation": "简要说明识别到了哪些关键规模参数，以及如何进行阶梯式构造的。"
            }
            """;

    /**
     * 用户提示词模板
     */
    private static final String USER_PROMPT_TEMPLATE = """
            题目名称：{title}
            题目描述：{description}
            标准代码：{standardCode}
            测试用例数量：{testcaseCount}
            时间限制：{timeLimit}ms
            空间限制：{memoryLimit}MB
            
            请基于以上信息生成对应的 Cyraon 脚本。
            """;

    /**
     * 执行 AI 分析并生成代码
     *
     * @param taskId 任务ID
     * @return 生成的代码结果 JSON
     */
    public String analyzeAndGenerate(Long taskId) {
        // 1. 从 Redis 获取原始任务数据
        String dataKey = RedisConstants.getTaskDataKey(taskId);
        String jsonData = redisTemplate.opsForValue().get(dataKey);
        if (jsonData == null) {
            log.error("任务数据在 Redis 中不存在或已过期: taskId={}", taskId);
            throw new RuntimeException("任务记录已过期，请手动创建新任务: " + taskId);
        }

        TaskSubmitDTO taskData = JSONUtil.toBean(jsonData, TaskSubmitDTO.class);
        String sys_prompt = getSysPromptTemplate();
        // 2. 调用 AI (使用 ChatClient)
        log.info("开始调用 AI 生成代码: taskId={}", taskId);

        String aiResult = chatClient.prompt()
                .system(sys_prompt)
                .user(u -> u.text(USER_PROMPT_TEMPLATE)
                        .param("title", taskData.getTitle())
                        .param("description", taskData.getProblemDescription())
                        .param("standardCode", taskData.getStandardCode())
                        .param("testcaseCount", taskData.getTestcaseCount())
                        .param("timeLimit", taskData.getTimeLimit())
                        .param("memoryLimit", taskData.getMemoryLimit()))
                .call()
                .content();

        // 3. 清理 AI 返回的格式（防止 AI 仍然带了 markdown 标签）
        aiResult = cleanAiResult(aiResult);

        // 4. 将结果存入 Redis
        String codeKey = RedisConstants.getTaskCodeKey(taskId);
        redisTemplate.opsForValue().set(codeKey, aiResult, 1, TimeUnit.HOURS);
        log.info("代码生成成功并已存入 Redis: taskId={}", taskId);

        return aiResult;
    }

    private String getSysPromptTemplate() {
        String systemPromptKey = RedisConstants.getSystemConfigKey(RedisConstants.getSystemPromptKey);
        String template = redisTemplate.opsForValue().get(systemPromptKey);
        if (StrUtil.isNotBlank(template)) return template;
        return SYSTEM_PROMPT;
    }

    private String cleanAiResult(String result) {
        if (result == null) return "";
        result = result.trim();
        if (result.startsWith("```json")) {
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
