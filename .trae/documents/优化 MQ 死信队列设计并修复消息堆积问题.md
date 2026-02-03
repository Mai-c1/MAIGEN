## 1. 统一 MQ 异常处理策略
- **移除手动死信发送**：删除 `TaskSubmitConsumer` 和 `TaskExecuteConsumer` 中手动向 `EXCHANGE_TASK_DLX` 发送 `TaskErrorDTO` 的代码，避免逻辑重叠。
- **标准化异常抛出**：统一在 `catch` 块中抛出 `AmqpRejectAndDontRequeueException` 或通过配置限制重试，确保失败消息通过 RabbitMQ 原生的 DLX 机制自动进入死信队列。

## 2. 修正 Sandbox 模块的堆积问题
- **配置重试限制**：在 `MAIGEN-sandbox` 和 `MAIGEN-analysis` 的 `application.yml` 中添加 RabbitMQ 重试配置，设置 `max-attempts` 和 `default-requeue-rejected: false`，防止消息无限重试导致队列卡死。
- **优化异常反馈**：在抛出异常前，通过 `TaskStatusConsumer` 发送最后一次失败状态更新，确保前端能即时感知。

## 3. 增强 API 模块的死信处理
- **幂等性检查**：在 `TaskDeadLetterConsumer` 处理死信时，增加对任务当前状态的检查，如果任务已经是 `FAILED` 状态则跳过，防止积分重复退还。
- **消息解析优化**：改进死信消息的解析逻辑，兼容多种可能进入死信的消息格式（Long, TaskResultDTO, TaskErrorDTO 等）。