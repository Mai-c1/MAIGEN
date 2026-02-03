## 问题分析
报错原因在于 `MAIGEN-api` 启动时，`TaskResultConsumer` 尝试监听队列 `maigen.sandbox.result`，但该队列在 RabbitMQ 服务器上并不存在。

在 Spring AMQP 中，如果使用了 `@RabbitListener` 监听某个队列，而该队列尚未被创建且代码中没有定义对应的 `Queue` Bean，Spring 就无法自动声明（创建）该队列，从而抛出 `404 NOT_FOUND` 错误。

## 解决方案
在公共模块 `MAIGEN-common-rabbitmq` 的配置类中显式定义业务队列的 Bean。这样，当任何依赖该模块的应用启动时，Spring 的 `RabbitAdmin` 会自动在 RabbitMQ 中创建这些缺失的队列。

## 实施步骤
1. **修改 RabbitMQ 配置**：
   - 在 [RabbitMQConfig.java](file:///d:/web/MAIGEN2/backend/MAIGEN-common/MAIGEN-common-rabbitmq/src/main/java/com/maigen/common/rabbitmq/config/RabbitMQConfig.java) 中添加两个业务队列的 Bean 定义：
     - `maigen.task.submit` (对应常量 `QUEUE_API_TO_ANALYSIS`)
     - `maigen.sandbox.result` (对应常量 `QUEUE_SANDBOX_TO_API`)
2. **验证修复**：
   - 重新运行 `MAIGEN-api`，确认启动过程中不再报 `DeclarationException` 错误，且能够成功连接并监听队列。
