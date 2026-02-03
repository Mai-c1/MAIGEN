---
name: "bean-copy-standard"
description: "强制使用 BeanUtil.copyProperties 进行对象转换。在层之间（VO/DTO/Entity）传输数据时调用。"
---

# Bean 复制标准

本技能强制使用 `cn.hutool.core.bean.BeanUtil` 进行对象属性复制，以确保一致性并减少样板代码。

## 规则

1.  **工具**: 使用 `cn.hutool.core.bean.BeanUtil`。
2.  **方法**: 使用 `BeanUtil.copyProperties(source, target)` 或 `BeanUtil.toBean(source, Target.class)`。
3.  **列表转换**: 使用 `BeanUtil.copyToList(sourceList, Target.class)`。
4.  **避免**: 除非需要复杂的转换，否则不要使用 `org.springframework.beans.BeanUtils` 或手动 setter。

## 示例

### 单个对象
```java
UserVO vo = new UserVO();
BeanUtil.copyProperties(userEntity, vo);
// 或者
UserVO vo = BeanUtil.toBean(userEntity, UserVO.class);
```

### 列表
```java
List<UserVO> voList = BeanUtil.copyToList(userEntityList, UserVO.class);
```
