package com.maigen.common.redis.constant;

/**
 * Redis Key 常量定义
 * 用于管理跨模块共享的 Redis Key 前缀
 */
public class RedisConstants {

    /**
     * 任务进度 Key 前缀
     * 格式: task:progress:{taskId}
     * 类型: String (存储状态码)
     * TTL: 1小时
     */
    public static final String TASK_PROGRESS_PREFIX = "task:progress:";

    /**
     * 任务详细数据负载 Key 前缀 (Claim Check Pattern)
     * 格式: task:data:{taskId}
     * 类型: String (存储 TaskSubmitDTO 的 JSON)
     * TTL: 1小时
     */
    public static final String TASK_DATA_PREFIX = "task:data:";

    /**
     * AI 生成代码脚本 Key 前缀
     * 格式: task:code:{taskId}
     * 类型: String (存储生成的代码或结构化 JSON)
     * TTL: 1小时
     */
    public static final String TASK_CODE_PREFIX = "task:code:";

    /**
     * 邮箱验证码 Key 前缀
     * 格式: auth:code:{email}
     * 类型: String
     * TTL: 5分钟
     */
    public static final String AUTH_CODE_PREFIX = "auth:code:";

    /**
     * 用户 Token Key 前缀 (Sa-Token 默认使用，此处仅做记录或自定义扩展)
     */
    public static final String SATOKEN_KEY_PREFIX = "satoken:login:token:";

    /**
     * 系统配置缓存 Key 前缀
     * 格式: system:config:{code}
     * 类型: String
     * TTL: 永不过期
     */
    public static final String SYSTEM_CONFIG_PREFIX = "system:config:";


    /**
     * AI工作流配置 Hash Key
     * 类型: Hash {workflowId: List<AiWorkflowStep>}
     */
    public static final String SYS_AI_WORKFLOWS_KEY = "sys:ai:workflows";

    /**
     * 前端可见的 AI 工作流列表 Key
     * 类型: String (List<AiWorkflow>)
     */
    public static final String SYS_AI_WORKFLOWS_PUBLIC_KEY = "sys:ai:workflows:public";

    /**
     * Cyaron 脚本提示词
     */
    public static String getSystemPromptKey = "SYSTEM_PROMPT";

    /**
     * 构造完整的 System Config Key
     * @param code 配置编码
     * @return 完整的 Key
     */
    public static String getSystemConfigKey(String code) {
        return SYSTEM_CONFIG_PREFIX + code;
    }

    /**
     * 构造完整的 Task Progress Key
     * @param taskId 任务ID
     * @return 完整的 Key
     */
    public static String getTaskProgressKey(Long taskId) {
        return TASK_PROGRESS_PREFIX + taskId;
    }

    /**
     * 构造完整的 Task Data Key
     * @param taskId 任务ID
     * @return 完整的 Key
     */
    public static String getTaskDataKey(Long taskId) {
        return TASK_DATA_PREFIX + taskId;
    }

    /**
     * 构造完整的 Task Code Key
     * @param taskId 任务ID
     * @return 完整的 Key
     */
    public static String getTaskCodeKey(Long taskId) {
        return TASK_CODE_PREFIX + taskId;
    }

    /**
     * 构造完整的 Auth Code Key
     * @param email 邮箱
     * @return 完整的 Key
     */
    public static String getAuthCodeKey(String email) {
        return AUTH_CODE_PREFIX + email;
    }
}
