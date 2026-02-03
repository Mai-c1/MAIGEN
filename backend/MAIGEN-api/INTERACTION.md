# MAIGEN-api 交互场景设计 (Interaction Scenarios)

本文档详细描述了 `MAIGEN-api` 模块在不同业务场景下的交互流程、接口定义及逻辑处理。

## 1. 用户认证与管理 (User Authentication & Management)

### 1.1 注册流程 (Registration)
1.  **发送验证码**
    *   **接口**: `POST /auth/send-code`
    *   **参数**: `email`
    *   **逻辑**:
        *   校验邮箱格式。
        *   检查 Redis 中 `auth:code:{email}` 是否存在且未过期（防止频繁发送）。
        *   生成 6 位数字验证码。
        *   存入 Redis (TTL 5分钟)。
        *   调用 EmailUtil 发送邮件。
2.  **用户注册**
    *   **接口**: `POST /auth/register`
    *   **参数**: `email`, `password`, `code`, `invitationCode` (可选)
    *   **逻辑**:
        *   校验验证码（对比 Redis）。
        *   校验邮箱是否已注册。
        *   创建用户：加密密码 (SaSecureUtil.md5BySalt)，生成唯一邀请码，初始积分 +50 (PointsConstants.REGISTER_REWARD)。
        *   **处理邀请逻辑**: 若 `invitationCode` 有效，查找邀请人，给邀请人 +20 积分，记录邀请关系 (`invitation` 表)。
        *   删除 Redis 验证码。

### 1.2 登录流程 (Login)
1.  **用户登录**
    *   **接口**: `POST /auth/login`
    *   **参数**: `username`, `password`
    *   **逻辑**:
        *   根据账号查询用户。
        *   校验密码。
        *   **Sa-Token 登录**: `StpUtil.login(userId)`。
        *   返回 Token 信息。

### 1.3 个人信息 (Profile)
1.  **获取信息**: `GET /user/info` (需登录)
2.  **修改信息**: `PUT /user/info` (昵称, 头像等)

---

## 2. 任务管理流程 (Task Management)

### 2.1 任务创建与提交 (Task Submission)
*   **接口**: `POST /task/create`
*   **参数**: `title`, `problemDescription`, `standardCode`, `testcaseCount`, `testcaseConfig` (JSON)
*   **逻辑**:
    1.  **积分检查**: 检查用户积分是否 >= 5 (PointsConstants.GENERATE_TASK_COST)。
    2.  **扣除积分**: 扣除 5 积分，记录 `points_record` (Source: CREATE_TASK)。
    3.  **保存任务**: 插入 `task` 表，状态设为 `WAITING` (0)，生成 `id`。
    4.  **初始化进度**: 在 Redis 中设置 `task:progress:{taskId}`，初始进度 0%。
    5.  **异步提交**: 组装 `TaskSubmitDTO` (包含 `taskId`)，发送到 RabbitMQ 队列 `maigen.task.submit`。
    6.  **返回**: 返回 `taskId` 给前端。

### 2.2 任务状态查询 (Polling)
*   **接口**: `GET /task/status/{taskId}`
*   **逻辑**:
    1.  **查缓存**: 优先读取 Redis `task:progress:{taskId}`。
    2.  **查数据库**: 若缓存未命中（任务已结束或过期），查询数据库 `task` 表状态。
    3.  **返回**: 状态 (status), 进度 (progress), 结果下载链接 (如果已完成)。

### 2.3 任务结果处理 (MQ Consumer)
*   **监听队列**: `maigen.sandbox.result`
*   **消息体**: `TaskResultDTO` (`taskId`, `downloadUrl`, `status`, `message`)
*   **逻辑**:
    1.  根据 `taskId` 更新 `task` 表：
        *   设置 `status` (4-完成 或 5-失败)。
        *   设置 `download_url` (MinIO 链接)。
        *   设置 `progress` = 100。
    2.  若成功，保存/更新 `generated_data` 表记录。
    3.  **更新缓存**: 更新 Redis `task:progress:{taskId}` 状态为完成，延长 TTL 以便前端最后一次轮询能获取到结果。

### 2.4 任务重试流程 (Retry)
*   **接口**: `POST /task/retry/{taskId}`
*   **逻辑**:
    1.  **权限校验**: 检查任务是否属于当前用户。
    2.  **状态校验**: 仅 `FAILED` 状态允许重试。
    3.  **重置状态**: `status` 设为 `PENDING` (0)，`progress` 设为 0，`retry_count` +1，清空 `error_message`。
    4.  **重新提交**: 构造 `TaskSubmitDTO` 重新发送至 RabbitMQ。

---

## 3. 积分与社区 (Points & Community)

### 3.1 每日签到 (Sign-in)
*   **接口**: `POST /points/sign-in`
*   **逻辑**:
    1.  **重复校验**: 检查 `user_sign_in` 表中今日是否已有记录。
    2.  **发放奖励**: 用户积分 +5 (PointsConstants.SIGN_IN_REWARD)。
    3.  **记录流水**: 增加 `points_record` (Source: SIGN_IN)。

### 3.2 广告激励 (Ad Reward)
*   **接口**: `POST /points/ad-reward`
*   **逻辑**:
    1.  **模拟发放**: 用户积分 +2 (PointsConstants.AD_REWARD)。
    2.  **记录流水**: 增加 `points_record` (Source: AD_REWARD)。

### 3.3 社区资源下载 (Download)
*   **接口**: `POST /community/{contentId}/download`
*   **逻辑**:
    1.  检查 `community_unlock` 表，是否已解锁该资源。
    2.  **若未解锁**:
        *   检查积分余额 >= 10 (PointsConstants.DOWNLOAD_COMMUNITY_DATA_COST)。
        *   **扣费**: 当前用户 -10 积分。
        *   **奖励**: 资源作者 +5 积分 (PointsConstants.DOWNLOAD_REWARD_AUTHOR)。
        *   **记录**: 插入 `community_unlock` 记录。
    3.  **获取链接**: 生成/获取 MinIO 的下载链接 (Presigned URL)。
    4.  返回下载链接。

---

## 4. 异常处理 (Error Handling)

*   **全局异常**: 使用 `GlobalExceptionHandler` 捕获。
*   **业务异常**: 抛出 `CustomException` (例如 "积分不足", "验证码错误")，返回对应的 Result 错误码。
