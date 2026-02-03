# 修复前端无法处理后端雪花 ID (Long 类型) 的问题

## 问题分析
JavaScript 的 `Number` 类型由于使用 64 位浮点数表示，其最大安全整数为 $2^{53} - 1$ (9007199254740991)。而后端的雪花 ID (Snowflake ID) 通常是 64 位长整型 (`Long`)，最大可达 $2^{63} - 1$，这会导致前端在接收 ID 时出现精度丢失，从而无法正确关联数据（如任务 ID、用户 ID 等）。

## 解决方案
在后端配置 Jackson 序列化规则，将所有的 `Long` 类型字段在返回给前端时自动转换为 `String` 类型。这种方式：
1. **全局生效**：无需在每个 DTO/VO 的字段上添加注解。
2. **前端友好**：前端作为字符串接收 ID，可以保证精度不丢失，且 JavaScript 能够轻松处理字符串形式的数字。
3. **反序列化兼容**：Jackson 默认支持将字符串形式的数字反序列化回后端的 `Long` 类型，因此不影响接口传参。

## 实施步骤
### 1. 创建 Jackson 配置类
在 `MAIGEN-api` 模块中创建 `com.maigen.api.config.JacksonConfig` 类。
- 使用 `Jackson2ObjectMapperBuilderCustomizer` 自定义 `ObjectMapper`。
- 配置 `Long.class` 和 `Long.TYPE` 的序列化器为 `ToStringSerializer`。
- 同时也配置 `BigInteger.class` 以防万一。

### 2. 验证
- 检查 `TokenVO` 中的 `userId`。
- 检查 `TaskDetailVO` 中的 `id`。
- 检查 `TaskStatusVO` 中的 `taskId`。
- 确保所有返回给前端的 ID 均变为字符串格式。

## 涉及文件
- [JacksonConfig.java](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/config/JacksonConfig.java) (新增)
