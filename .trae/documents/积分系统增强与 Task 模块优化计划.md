# 实施计划：积分系统增强与 Task 模块优化

## 1. 积分系统功能实现
### 目标
增加积分余额查询和流水明细功能，并确保所有积分变动都有据可查。

### 实施步骤
1.  **创建 PointsController**:
    - `GET /points/balance`: 查询当前用户积分余额。
    - `GET /points/records`: 分页查询当前用户积分变动记录。
2.  **完善 PointsRecordService**:
    - 实现 `getBalance(userId)`：从 `User` 表获取最新积分。
    - 实现 `getRecordPage(userId, pageQuery)`：基于 `PointsRecord` 表的分页查询。
3.  **修复 Task 创建时的积分记录缺失**:
    - 在 `TaskServiceImpl.createTask` 中，除了扣减 `User.points`，还需向 `PointsRecord` 表插入一条消费记录。
4.  **定义 VO/DTO**:
    - `PointsBalanceVO`
    - `PointsRecordVO` (包含：变动数值、变动类型、描述、关联ID、创建时间)

---

## 2. Task 模块功能检查与优化计划
### 现状检查结果
- **核心流程已建立**: 任务创建 -> MQ发送 -> 结果监听 的闭环基本成型。
- **待补充功能**:
    - **任务列表**: 用户无法查看历史提交的任务列表。
    - **结果处理**: `handleTaskResult` 目前是 TODO，生成的测试点数据尚未关联回任务。
    - **积分闭环**: 积分变动未记录到流水表。
- **优化空间**:
    - **状态管理**: 建议使用枚举管理任务状态（0-待处理, 4-完成等）。
    - **异常退款**: 如果任务处理失败（AI生成失败或沙箱报错），应退还积分。

### 优化方案步骤
1.  **完善 `TaskController`**:
    - 增加 `GET /task/list`: 分页查询当前用户的历史任务。
    - 增加 `DELETE /task/{taskId}`: 删除/隐藏任务记录。
2.  **实现 `handleTaskResult` 逻辑**:
    - 更新 `Task` 表状态和完成时间。
    - 将 `Analysis` 或 `Sandbox` 返回的结果写入 `generated_data` 或 `task_testcase` 表。
3.  **引入状态枚举**:
    - 创建 `TaskStatusEnum`，替换硬编码的数字状态码。
4.  **积分退还逻辑**:
    - 在 `handleTaskResult` 中，若状态为失败，执行积分返还并记录流水。

您是否同意此计划？如果同意，我将先开始“积分系统”的实现。
