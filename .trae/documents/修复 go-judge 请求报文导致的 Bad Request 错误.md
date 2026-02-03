## 1. 优化 DTO 定义
在 `GoJudgeRequest.java` 及其内部类（`Cmd`, `FileItem`, `FileContent`）上添加 `@JsonInclude(JsonInclude.Include.NON_NULL)` 注解。
移除 `GoJudgeRequest` 顶层的 `copyIn` 字段，确保结构与测试脚本一致。

## 2. 修正请求构造逻辑
修改 `TaskExecuteService.java` 中的 `executeTask` 方法：
- 确保 `GoJudgeRequest.Cmd` 的构造中不再包含 `clockLimit`。
- 确认 `files` 数组中第一个元素的构造不包含任何多余字段（只有 `content: ""`）。

## 3. 验证与日志
在 `GoJudgeClient.java` 或 `TaskExecuteService.java` 中增加 Debug 级别的日志，输出最终发送给 `go-judge` 的 JSON 报文，方便二次排查。