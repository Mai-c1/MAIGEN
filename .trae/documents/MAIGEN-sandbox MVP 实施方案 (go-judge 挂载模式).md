# MAIGEN-sandbox MVP 开发方案

您好！根据我们的深入核对，`MAIGEN-sandbox` 将采用 **go-judge (executorserver)** 挂载模式实现高性能数据生成。以下是最终确定的实施方案：

## 1. 核心流程与架构
- **数据一致性**：继续遵循 Claim Check 模式，MQ 仅传递 `Long taskId`。
- **沙箱调度**：通过 `localhost:5050/run` 调用 go-judge 接口。
- **文件挂载**：宿主机 `/maigen/share/data` 映射至沙箱 `/output/`，实现零 Base64 开销的本地 I/O 交付。
- **执行调度**：在 Sandbox 中动态构造 `run.sh`，通过字符串替换注入 `taskId`。

## 2. 详细实施步骤

### 第一阶段：AI 生成策略对齐 (Analysis 模块)
- **Prompt 优化**：更新 `AiAnalysisService` 的提示词，强制 AI 生成的 `gen.py` 必须将 `.in` 文件输出到 `./data/` 目录。

### 第二阶段：沙箱基础建设 (Sandbox 模块)
- **依赖与配置**：
    - `pom.xml`: 引入 `MAIGEN-common-miniIO` 和 `WebFlux` (用于调用沙箱 API)。
    - `application.yml`: 配置 go-judge 地址及挂载路径。
- **GoJudgeClient 封装**：
    - 实现对 `go-judge` 标准 API 的请求封装。
    - 预设 `mounts` 配置，映射 `/bin`, `/usr`, `/lib` 等系统路径及 `/output` 交付路径。

### 第三阶段：核心执行逻辑 (`TaskExecuteService`)
- **脚本构造**：将 Redis 中的 `std.cpp` 和 AI 生成的 `gen.py` 与动态生成的 `run.sh` 组装。
- **沙箱运行**：调用 `go-judge` 执行 `run.sh`（包含编译、数据生成、标程运行、打包）。
- **结果交付**：
    - 校验沙箱 `exitStatus`。
    - 若成功，从物理路径 `/maigen/share/data/` 读取 ZIP 上传 MinIO。
    - 清理物理路径下的 ZIP 文件。

### 第四阶段：全链路闭环
- **Consumer 重构**：修改 `TaskExecuteConsumer` 接收 `Long taskId` 并调用服务。
- **异常处理**：统一使用 `TaskErrorDTO` 上报死信，并发送 `FAILED` 状态更新。

核对点确认：
- 超时控制：30s。
- 权限：Sandbox 进程具备物理路径的读写删除权限。
- 协议：全链路 Long taskId 传输。

方案已就绪，我将立即开始全权开发。
