---
name: "redis-common-usage"
description: "标准化共享 Key 的 Redis 用法。在实现跨多个模块的缓存或基于 Redis 的功能时调用。"
---

# Redis 通用用法

当多个模块（例如：API、Analysis）需要访问相同的 Redis Key 时，在 `MAIGEN-common-redis` 中定义常量和 Key 生成逻辑。

## 1. Key 定义
在 `MAIGEN-common-redis` 模块内的 `com.maigen.common.redis.constant.RedisConstants` 中定义 Key。

**示例**:
```java
public class RedisConstants {
    public static final String TASK_PROGRESS_PREFIX = "task:progress:";
    
    public static String getTaskProgressKey(Long taskId) {
        return TASK_PROGRESS_PREFIX + taskId;
    }
}
```

## 2. 用法
在业务逻辑中使用 `RedisConstants` 中的常量和辅助方法。

**示例**:
```java
// 设置进度
String key = RedisConstants.getTaskProgressKey(taskId);
redisUtil.set(key, progressObj, 3600); // 1 小时 TTL
```

## 3. 好处
- **一致性**: 确保所有模块读/写完全相同的 Key。
- **可维护性**: Key 格式和 TTL 在一个地方管理。
