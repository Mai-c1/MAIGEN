# 任务进度条（Progress）优化方案

当前任务进度更新过于“离散”且“跳跃”，主要表现为：
- **长时间停滞**：AI 分析（10%）和沙箱验证（70%）是耗时最长的阶段，但进度条在这两个阶段会长时间静止，给用户一种系统卡死的错觉。
- **进度跳变**：进度值仅在状态切换时（10% -> 40% -> 70% -> 100%）发生大跨度跳跃，不够丝滑。

## 优化思路：细粒度上报 + 进度范围化

### 1. 后端：细化进度里程碑
将原本单一的状态百分比拆分为多个内部步骤的上报，确保进度条能反映任务的真实流动。

| 任务阶段 | 内部步骤 | 建议进度 (%) | 状态描述 (Message) |
| :--- | :--- | :--- | :--- |
| **Pending** | 任务已提交 | 0 -> 5 | 任务排队中... |
| **Analyzing** | 准备 AI 提示词 | 10 | 正在拉取数据并准备分析... |
| | 调用 AI 引擎 | 20 | AI 正在分析题目并生成代码... |
| | 处理 AI 返回结果 | 55 | 正在解析并存储生成结果... |
| **Generating** | 发送至沙箱 | 60 | 代码生成完成，准备执行验证... |
| **Verifying** | 启动沙箱环境 | 70 | 正在初始化沙箱隔离环境... |
| | 执行测试数据 | 85 | 正在运行代码验证数据... |
| | 整理报告 | 95 | 正在整理最终验证报告... |
| **Completed** | 任务完成 | 100 | 任务处理成功 |

### 2. 技术实现步骤

#### 第一步：重构 TaskStatusEnum
- 调整 [TaskStatusEnum.java](file:///d:/web/MAIGEN2/backend/MAIGEN-common/MAIGEN-common-core/src/main/java/com/maigen/common/core/enums/TaskStatusEnum.java) 中的 `defaultProgress`，为各阶段留出增长空间。

#### 第二步：增强 Analysis 模块进度上报
- 在 [TaskSubmitConsumer.java](file:///d:/web/MAIGEN2/backend/MAIGEN-analysis/src/main/java/com/maigen/analysis/mq/TaskSubmitConsumer.java) 中，将原本的一处状态更新拆分为三处：
  1. 调用 `AiAnalysisService` 前上报 10% (Preparing)。
  2. 在 `AiAnalysisService` 内部或调用后上报 20% (AI Generating)。
  3. 存入 Redis 后上报 55% (Processing Result)。

#### 第三步：增强 Sandbox 模块进度上报
- 在 [TaskExecuteConsumer.java](file:///d:/web/MAIGEN2/backend/MAIGEN-sandbox/src/main/java/com/maigen/sandbox/mq/TaskExecuteConsumer.java) 中增加中间上报：
  1. 沙箱启动前 70% (Initializing)。
  2. 沙箱执行完成后、发送结果前 95% (Finalizing)。

#### 第四步：前端“虚拟进度”建议（可选）
- 在前端轮询时，如果后端进度没有变化，前端可以每隔 1-2 秒自动微增 1%，直到达到当前阶段的上限（如 10% 增加到 19% 就停下），等待后端的真实跳变。

## 待确认事项
1. 是否需要在 `TaskStatusEnum` 中直接定义这些细分步骤，还是直接在 Consumer 代码中硬编码这些进度值？（建议在 Consumer 中根据业务细分上报，保持 Enum 简洁）
2. AI 分析阶段是否需要更细的进度（例如根据 AI 输出的流式长度来估算）？目前建议先按上述固定步长实现。

您是否同意按照上述“细粒度上报”的方案进行后端代码调整？
