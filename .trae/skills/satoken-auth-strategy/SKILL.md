---
name: "satoken-auth-strategy"
description: "指导 Sa-Token 注解用于授权的使用。在保护 API 端点或实现权限检查时调用。"
---

# Sa-Token 授权策略

使用 Sa-Token 注解来保护 Controller 方法。

## 1. 登录检查
使用 `@SaCheckLogin` 确保用户已登录。

```java
@SaCheckLogin
@GetMapping("/info")
public Result<UserVO> getUserInfo() { ... }
```

## 2. 角色检查
使用 `@SaCheckRole` 确保用户拥有特定角色。

```java
@SaCheckRole("admin")
@PostMapping("/user/ban")
public Result<Void> banUser() { ... }
```

## 3. 权限检查
使用 `@SaCheckPermission` 进行细粒度的权限控制。

```java
@SaCheckPermission("user:edit")
@PutMapping("/user")
public Result<Void> updateUser() { ... }
```

## 4. 模式
- `mode = SaMode.AND`: 必须拥有所有权限 (默认)。
- `mode = SaMode.OR`: 必须拥有至少一个权限。

```java
@SaCheckPermission(value = {"user:add", "user:edit"}, mode = SaMode.OR)
```
