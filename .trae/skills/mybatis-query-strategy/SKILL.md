---
name: "mybatis-query-strategy"
description: "指导 MyBatis-Plus LambdaQuery 和 XML Mapper 之间的选择。在编写涉及数据库查询的 Service 或 DAO 层逻辑时调用。"
---

# MyBatis 查询策略

本技能定义了何时使用 MyBatis-Plus Lambda 包装器与自定义 XML Mapper。

## 1. 简单查询 -> LambdaQuery
对于单表操作和简单条件，使用 `LambdaQueryWrapper` 或 `LambdaUpdateWrapper`。

**场景**:
- 根据 ID/列查找
- 简单的 AND/OR 条件
- 单表分页
- 简单更新

**示例**:
```java
// 查找 status = 1 且 age > 18 的用户
List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
    .eq(User::getStatus, 1)
    .gt(User::getAge, 18));
```

## 2. 复杂查询 -> XML Mapper
对于多表连接、复杂聚合或性能关键的查询，使用自定义 XML SQL。

**场景**:
- `JOIN` 操作 (例如：用户左连接角色)
- 复杂分组 / 聚合
- 嵌套子查询
- 需要特定 SQL 优化

**示例**:
*UserMapper.java*:
```java
IPage<UserVO> selectUserPage(Page<?> page, @Param("param") UserQueryDTO param);
```

*UserMapper.xml*:
```xml
<select id="selectUserPage" resultType="com.maigen.api.model.vo.UserVO">
    SELECT u.*, r.role_name 
    FROM user u 
    LEFT JOIN user_role ur ON u.id = ur.user_id
    LEFT JOIN role r ON ur.role_id = r.id
    WHERE u.status = 1
</select>
```
