# MAIGEN-sandbox MVP 最终开发报告与实施方案

根据您的反馈与 `go-judge` 技术规范，`MAIGEN-sandbox` 将采用 **高性能共享目录挂载方案**。该方案通过物理路径直接交付数据，规避了大数据量下的 Base64 编解码开销，是目前沙箱系统的最优实践。

## 1. 核心技术架构
- **通信协议**: `MAIGEN-sandbox` 与 `go-judge` 协同运行于同一容器，通过 `http://localhost:5050/run` 进行高速通信。
- **高性能交付**: 
    - 宿主机共享目录：`/maigen/share/data`。
    - 沙箱挂载点：`/output` (Bind Mount, RW)。
    - 流程：沙箱直接将 ZIP 打包至 `/output` -> 宿主机直接读取物理文件上传 MinIO -> 物理删除。
- **环境预装**: Dockerfile 将集成 `g++`, `python3`, `cyaron`, `zip` 等全套工具链。

## 2. 实施路线图

### 第一阶段：AI 提示词约束更新 (Analysis 模块)
- **目标**: 确保生成的 `gen.py` 能够与沙箱调度脚本完美契合。
- **变更**: 修改 `AiAnalysisService` 的 System Prompt，强制要求 AI 在 Python 代码中将所有 `.in` 文件生成至当前目录下的 `data/` 文件夹中。

### 第二阶段：沙箱引擎实现 (Sandbox 模块)
- **依赖升级**: 集成 `MAIGEN-common-miniIO`，确保具备文件上传能力。
- **脚本调度器**:
    - 在内存中动态构造 `run.sh`，完成 `{TASK_ID}` 的字符串替换。
    - 设置沙箱参数：`cpuLimit: 30s`, `clockLimit: 30s`。
- **Go-Judge 交互**:
    - 构造 `mounts` 报文：将物理路径 `/maigen/share/data` 挂载为沙箱内的 `/output`。
    - 实现同步请求与结果校验逻辑（解析 `exitStatus` 和 `stderr`）。

### 第三阶段：全链路闭环
- **重构消费者**: `TaskExecuteConsumer` 接入引擎，实现从“收到 taskId”到“回传下载链接”的完整链路。
- **异常穿透**: 捕获沙箱内部的编译错误或 Python 运行时异常，通过 `TaskErrorDTO` 发送至死信队列，并同步更新 API 模块的任务状态。

## 3. 性能优化要点
- **零拷贝思想**: 利用共享目录，Java 进程通过文件流 (`InputStream`) 直接对接 MinIO SDK，无需将数 GB 的数据加载进 JVM 内存。
- **并行执行**: 利用 RabbitMQ 的并发消费特性，支持多任务同时在沙箱中编译与运行。

如果您对这份最终报告无异议，我将立即开始编码实施。
