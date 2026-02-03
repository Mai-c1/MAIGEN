# MAIGEN-analysis MVP 开发实施

方案已确认。我将按照以下步骤全权执行开发工作：

1. **公共模块增强**：完善 `TaskExecuteDTO`、`RedisConstants` 及 MQ 序列化配置。
2. **MAIGEN-analysis AI 接入**：集成 Spring AI Alibaba，配置通义千问 API。
3. **核心逻辑实现**：编写 Prompt，实现基于 Claim Check 模式的 AI 脚本生成服务。
4. **任务流重构**：更新消费者逻辑，确保全链路轻量化消息传递。
5. **代码清理**：同步优化 `MAIGEN-api` 中的重试逻辑。

准备开始编码。
