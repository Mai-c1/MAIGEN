## 问题分析
1. **异常未被捕获**：前端返回的是 Spring Boot 默认的 500 错误 JSON，说明抛出的 `CustomException` 没能进入任何全局异常处理器的 `ExceptionHandler` 方法。
2. **扫描路径问题**：`MAIGEN-api` 的主类在 `com.maigen.api` 包下，而 `MAIGEN-common-core` 中的 `GlobalExceptionHandler` 在 `com.maigen.common.core.exception` 包下。默认情况下，Spring Boot 不会扫描 common 模块中的异常处理器。
3. **API 模块异常处理器不完整**：`MAIGEN-api` 模块目前的 `GlobalExceptionHandler` 只处理了 Sa-Token 的登录/权限异常，缺少对业务异常（`CustomException`）和系统通用异常（`Exception`）的处理逻辑。

## 实施步骤
### 1. 完善 MAIGEN-api 异常处理器
在 `MAIGEN-api` 模块的 [GlobalExceptionHandler.java](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/exception/GlobalExceptionHandler.java) 中增加以下处理逻辑：
- 增加对 `CustomException` 的捕获，提取其中的 `message` 和 `code` 并通过 `SaResult` 返回。
- 增加对通用 `Exception` 的捕获，返回统一的“系统繁忙”提示，避免暴露内部错误堆栈。
- 增加对 `MethodArgumentNotValidException` 的捕获，处理 JSR303 参数校验异常。

### 2. 统一返回规范
确保所有新增的异常处理方法都使用 `cn.dev33.satoken.util.SaResult` 作为返回对象，以符合 `MAIGEN-api` 的接口规范。

## 验证计划
- 再次触发“今天已经签到过了”的业务逻辑。
- 检查前端收到的响应，预期格式应为：
  ```json
  {
      "code": 400,
      "msg": "今天已经签到过了",
      "data": null
  }
  ```
- 模拟其他代码异常，验证是否返回了统一的“系统繁忙”提示。