# MAIGEN-api 模块说明

`MAIGEN-api` 是系统的核心后端服务，负责处理前端请求、业务逻辑处理、数据库操作以及与其他模块的消息交互。

## 1. 架构决策与规范 (Confirmed Architecture)

根据需求分析与确认，本项目遵循以下核心架构原则：

### 1.1 模块交互
- **全异步 MQ 驱动**: 
  - `MAIGEN-api` **不直接调用** `MAIGEN-analysis` 或 `MAIGEN-sandbox`。
  - **任务提交 (API -> Analysis)**: API 接收用户请求，生成任务 ID，将 ID 发送到 MQ。Analysis 模块消费消息，根据 ID 从 Redis/DB 获取任务详情并开始处理。
  - **结果回传 (Sandbox -> API)**: 最终代码执行完毕后，Sandbox 将任务 ID 和下载链接发送到 MQ。API 模块消费消息，更新任务状态为完成，并保存下载链接。
- **职责边界**:
  - API 层只负责“提交任务”和“接收最终结果”，不关心 AI 内部流程（题目分析->策略->生成->评审）。
  - Sandbox 层作为纯粹的执行单元，通过 MQ 返回最终结果。

### 1.2 数据与缓存
- **进度查询**: 前端采用 **轮询 (Polling)** 机制。
- **缓存优先**: 任务状态查询接口必须优先查询 Redis 缓存，减少数据库压力。
- **支付**: 暂不接入第三方支付，仅预留后台人工充值入口。

## 2. RabbitMQ 队列设计 (API 相关)

仅列出与 `MAIGEN-api` 直接交互的队列。常量定义在 `MAIGEN-common-rabbitmq` 中。

| 交互方向 | 队列常量 (RabbitMQConstants) | 队列名称 | 消息体 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| **API -> Analysis** | `QUEUE_API_TO_ANALYSIS` | `maigen.task.submit` | `TaskSubmitDTO` | API 生产者，Analysis 消费者。仅传递 `taskId`。 |
| **Sandbox -> API** | `QUEUE_SANDBOX_TO_API` | `maigen.sandbox.result` | `TaskResultDTO` | Sandbox 生产者，API 消费者。传递 `taskId`, `downloadUrl` 等。标志任务流程结束。 |

## 3. 开发规范 (Coding Standards)

- **返回值**: Controller 方法统一使用 `Result<T>` (from `com.maigen.common.core.model`)。
- **对象拷贝**: 使用 `BeanUtil.copyProperties` (Hutool) 进行 DTO/VO/Entity 转换。

## 4. 核心功能模块

### 4.1 用户与权限 (User & Auth)
- 基于 Sa-Token 实现认证。
- RBAC 权限控制。
- 积分账户管理（充值、消费记录）。

### 4.2 任务管理 (Task)
- **任务提交**: 接收用户配置 -> 落库(Status=WAITING) -> 发送 MQ 消息。
- **状态更新**: 监听 Sandbox 完成消息 -> 更新 DB -> 更新 Redis。
- **查询**: 读 Redis 缓存中的任务进度和状态。

### 4.3 社区与资源 (Community)
- MinIO 文件存储集成。
- 社区内容分享、审核流程。
- 积分购买/下载机制。

## 5. 开发计划 (Roadmap)

1. **基础建设**: 
   - 配置 MySQL, Redis, RabbitMQ, MinIO 连接。
   - 引入 Sa-Token 依赖并配置。
2. **Common 模块**: 
   - 确保 `Result<T>`, `TaskSubmitDTO`, `TaskResultDTO`, `RabbitMQConstants` 已就绪。
3. **用户模块开发**: 
   - 注册、登录、个人信息。
4. **任务模块开发**:
   - 任务创建接口。
   - MQ 生产者（发送任务）。
   - MQ 消费者（接收结果）。
   - 状态查询接口（Redis）。