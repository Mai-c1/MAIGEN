---
name: modification-planner
description: 强制执行“修改前确认”的工作流程。当用户请求任何代码修改、重构或功能实现时调用。
---

# 修改规划器

本技能强制执行严格的代码修改工作流程。你必须 **规划**，然后 **确认**，最后才 **执行**。

## 工作流程步骤

1.  **分析与规划**
    *   理解用户的请求。
    *   确定所有需要创建、修改或删除的文件。
    *   起草清晰、分步的计划。
    *   *自我纠正*: 检查计划是否符合现有项目模式（例如：特定的 Auth 逻辑、架构约束）。

2.  **展示计划与确认**
    *   **关键**: 在编写任何代码之前，你必须停下来征求用户的确认。
    *   使用 `AskUserQuestion` 工具。
    *   清晰地格式化计划：
        *   **文件**: `path/to/file`
        *   **动作**: 创建 / 修改 / 删除
        *   **详情**: 变更的简要描述（例如：“添加 `checkLogin` 方法”）。

3.  **执行**
    *   只有在用户选择“是”或给予明确确认后。
    *   使用 `Write`、`SearchReplace` 等工具执行计划的变更。

## 何时调用
*   用户说“修复这个 bug”。
*   用户说“添加这个功能”。
*   用户说“重构这个类”。
*   用户说“把 X 改成 Y”。

## 示例

**用户**: "给 UserService 添加一个 `deleteUser` 方法。"

**模型 (调用 modification-planner)**:
"我已经分析了请求。计划如下：
1.  **修改** `UserService.java`: 添加 `deleteUser(Long id)` 接口。
2.  **修改** `UserServiceImpl.java`: 实现 `deleteUser`。添加在 `UserRoleService` 中删除关联角色的逻辑。
3.  **修改** `UserController.java`: 添加 `DELETE /user/{id}` 端点，并加上 `@SaCheckPermission("user:delete")`。

是否继续？"

**(用户确认)** -> **模型执行变更。**
