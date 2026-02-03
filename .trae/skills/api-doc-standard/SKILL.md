---
name: "api-doc-standard"
description: "使用 Swagger/OpenAPI 注解强制执行 API 文档标准。在编写或修改 Controller、Entity、DTO 或 VO 时调用。"
---

# API 文档标准

本技能确保所有 API 相关类都使用 OpenAPI 3 注解 (Knife4j/Swagger) 进行正确记录。

## 1. Controller 文档
Controller 必须使用 `@Tag` 进行分组。

```java
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录和个人资料管理 API")
public class UserController { ... }
```

## 2. API 操作文档
Controller 方法必须使用 `@Operation` 描述端点。

```java
import io.swagger.v3.oas.annotations.Operation;

@Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
@GetMapping("/info")
public Result<UserVO> getUserInfo() { ... }
```

## 3. 模型文档 (Entity/DTO/VO)
类和字段必须使用 `@Schema` 注解。

### 类级别
```java
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户登录请求对象")
public class LoginDTO { ... }
```

### 字段级别
```java
@Schema(description = "用户邮箱地址", example = "test@example.com")
private String email;

@Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
private String password;
```

## 4. 检查清单
- [ ] Controller 有 `@Tag` 吗？
- [ ] 方法有 `@Operation(summary)` 吗？
- [ ] DTO/VO/Entity 有 `@Schema(description)` 吗？
- [ ] 字段有 `@Schema(description)` 吗？
