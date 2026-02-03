# MAIGEN-analysis 模块优化方案

根据您的反馈，我将对 `MAIGEN-analysis` 模块进行重构和优化。核心思路是：**简化消息传递、统一状态标准、升级 AI 接入方式、清理冗余配置**。

## 1. 依赖与配置优化
- **Spring AI 升级**: 将 `spring-ai-alibaba-starter` 升级至 `1.0.0-M6.1`，并引入 `spring-ai-core`，以支持最新的 `ChatClient` API。
- **配置清理**: 
    - 移除 `MAIGEN-analysis` 中重复的 RabbitMQ 和 Redis 配置，改由 `common` 模块通过 `@PropertySource` 自动加载。
    - 将通义千问 API Key 配置为 `${QWEN_API_KEY}`，直接从环境变量中读取。

## 2. 消息传递简化 (Claim Check Pattern)
- **移除冗余 DTO**: 删除仅包含 `taskId` 的 `TaskMessageDTO` 和 `TaskExecuteDTO` 类。
- **轻量级传输**: 修改 API、Analysis 和 Sandbox 模块的 MQ 交互，直接传递 `Long taskId`。这符合“MQ 只传递最少信息”的原则，避免不必要的类创建。

## 3. AI 服务重构 (AiAnalysisService)
- **接入 ChatClient**: 按照官方最佳实践，通过 `ChatClient.Builder` 构建客户端，替代原有的 `ChatModel`。
- **Prompt 优化**: 保持原有的提示词逻辑，但使用 `ChatClient` 的链式调用进行封装，提高代码可读性和稳定性。

## 4. 状态更新标准化
- **统一枚举使用**: 强制 `sendStatusUpdate` 方法接收 `TaskStatusEnum` 对象而非 `Integer` 魔法数字，确保全链路状态码的一致性。
- **清理混乱枚举**: 明确 `TaskStatusEnum` 为主状态标准，`TaskStageStatusEnum` 仅用于前端 UI 阶段展示的映射逻辑。

## 实施步骤
1. 修改 `MAIGEN-common-core`，删除 `TaskMessageDTO` 和 `TaskExecuteDTO`。
2. 更新 `MAIGEN-analysis` 的 `pom.xml` 和 `application.yml`。
3. 重构 `AiAnalysisService`，接入 `ChatClient`。
4. 更新 `TaskSubmitConsumer` (Analysis) 和 `TaskExecuteConsumer` (Sandbox) 的监听器，接收 `Long taskId`。
5. 更新 `TaskServiceImpl` (API)，直接发送 `taskId`。
6. 统一 `sendStatusUpdate` 的入参为 `TaskStatusEnum`。

如果您确认该方案，我将开始执行。
