---
name: "standard-pagination"
description: "强制使用 PageDTO 和 PageQuery 进行标准分页。在实现或修改分页逻辑时调用。"
---

# 标准分页策略

本技能强制执行项目的标准分页模式，使用 `MAIGEN-api` 模块下的 `PageQuery` 和 `PageDTO`。

## 要求

1.  **输入 (DTO)**: 所有分页请求 DTO 必须继承 `com.maigen.api.model.dto.PageQuery`。
    - 该类提供了 `page` (页码), `pageSize` (页大小), `sortBy` (排序字段), `isAsc` (是否升序) 字段。
    - 该类提供了 `toMpPage()` 方法，可直接转换为 MyBatis-Plus 的 `Page` 对象。
2.  **Service 层**: 使用 `dto.toMpPage()` 构建 MyBatis-Plus 的 `Page` 对象。
3.  **输出 (VO)**: 所有分页响应必须使用 `com.maigen.api.model.dto.PageDTO`。
    - 使用 `PageDTO.of(page, convertor)` 来包装结果。

## 示例

### 请求 DTO
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {
    private String username;
}
```

### Service 实现
```java
public PageDTO<UserVO> queryUsers(UserQueryDTO dto) {
    // 1. 构建 MP Page 对象
    Page<User> page = dto.toMpPage();
    
    // 2. 查询
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    // ... 条件
    this.page(page, wrapper);
    
    // 3. 返回 PageDTO (自动转换 Entity -> VO)
    return PageDTO.of(page, user -> {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    });
}
```
