# MAIGEN-api 功能规格与接口文档 (Completed Features)

本文档总结了 `MAIGEN-api` 模块目前已完成的核心功能、接口规范及业务逻辑。

---

## 1. 用户认证模块 (Auth)
基于 Sa-Token 实现的权限认证体系。

| 接口名称 | 请求路径 | 方法 | 描述 |
| :--- | :--- | :--- | :--- |
| 发送验证码 | `/auth/send-code` | `POST` | 发送邮箱验证码 (Redis 5min 有效) |
| 用户注册 | `/auth/register` | `POST` | 支持邀请码，初始奖励 50 积分 |
| 用户登录 | `/auth/login` | `POST` | 返回 Sa-Token 令牌 |

**业务逻辑与注意事项**：
- **密码加密**：使用 `SaSecureUtil.md5BySalt` 存储。
- **邀请机制**：注册成功后自动为邀请人增加 20 积分，被邀请人增加 5 积分。

---

## 2. 任务管理模块 (Task)
系统的核心功能，采用异步 MQ 驱动。

| 接口名称 | 请求路径 | 方法 | 描述 |
| :--- | :--- | :--- | :--- |
| 创建任务 | `/task/create` | `POST` | 提交题目及代码，扣除 5 积分 |
| 查询进度 | `/task/status/{taskId}` | `GET` | 优先读 Redis 缓存，完成后返回下载 URL |
| 任务列表 | `/task/list` | `GET` | 分页查询当前用户的所有历史任务 |
| 任务重试 | `/task/retry/{taskId}` | `POST` | 对 FAILED 状态的任务发起重试 |

**业务逻辑与注意事项**：
- **异步流程**：API 创建任务后发送消息至 `maigen.task.submit` 队列，由 Analysis 模块处理。
- **失败退款**：若任务最终失败，系统会自动执行积分退还逻辑并记录流水。
- **重试机制**：重试会清空之前的 `error_message` 并重置 `progress`，增加 `retry_count` 计数。
- **详细原因**：如果任务失败，`status` 接口会返回 `errorMessage` 字段供前端展示。

---

## 3. 个人中心 (User Profile)
用户资料维护与安全设置。

| 接口名称 | 请求路径 | 方法 | 描述 |
| :--- | :--- | :--- | :--- |
| 个人信息 | `/user/info` | `GET` | 获取当前登录用户的详细资料 |
| 修改资料 | `/user/update` | `PUT` | 修改昵称、头像 |
| 修改密码 | `/user/password` | `POST` | 校验旧密码并更新新密码 |

---

## 4. 积分激励系统 (Points)
用户资产与活跃度管理。

| 接口名称 | 请求路径 | 方法 | 描述 |
| :--- | :--- | :--- | :--- |
| 查询余额 | `/points/balance` | `GET` | 获取当前可用积分 |
| 流水明细 | `/points/records` | `GET` | 分页查询积分变动记录 |
| 每日签到 | `/points/sign-in` | `POST` | 每日一次，奖励 5 积分 |
| 广告激励 | `/points/ad-reward` | `POST` | 模拟观看广告，奖励 2 积分 |

**业务逻辑与注意事项**：
- **签到校验**：基于 `user_sign_in` 表进行日期唯一性校验，防止重复领取。
- **流水闭环**：所有积分变动（扣费、退款、签到、广告）必须记录 `points_record`。

---

## 5. 社区互动模块 (Community)
提供资源分享、发现与社交互动能力。

| 接口名称 | 请求路径 | 方法 | 描述 |
| :--- | :--- | :--- | :--- |
| 分享内容 | `/community/share` | `POST` | 分享题目与数据，支持分类标签 |
| 内容列表 | `/community/list` | `GET` | 分页、分类、标签筛选，多种排序 |
| 内容详情 | `/community/detail/{id}` | `GET` | 查看详情（自动增加浏览量） |
| 下载资源 | `/community/{id}/download` | `POST` | 积分解锁并下载，支持作者奖励 |
| 点赞/取消 | `/community/like/{id}` | `POST` | 幂等点赞操作 |
| 内容评分 | `/community/rate` | `POST` | 1-5 分评分，自动更新平均分 |
| 分类列表 | `/category/list` | `GET` | 获取所有可选分类 |
| 标签列表 | `/tag/list` | `GET` | 获取所有可选标签 |

---

## 6. 管理后台 (Admin)
仅限具有“管理员”角色的用户访问。

| 接口名称 | 请求路径 | 方法 | 描述 |
| :--- | :--- | :--- | :--- |
| 审计日志 | `/admin/logs` | `GET` | 全系统操作日志审计 |
| 任务监控 | `/admin/tasks` | `GET` | 监控全站任务执行情况 |
| 用户管控 | `/admin/user/{id}/status` | `PUT` | 禁用或启用特定用户 |

---

## 7. 安全与运维 (Security & Ops)
| 功能名称 | 实现方式 | 描述 |
| :--- | :--- | :--- |
| 操作审计 | `@Log` 注解 + AOP | 自动记录关键操作的 IP、参数、耗时及状态 |
| 异常拦截 | `GlobalExceptionHandler` | 统一业务异常 (`CustomException`) 的返回格式 |
| 接口限流 | `@RateLimit` + Redis | 针对高频接口进行频率限制（如 1小时内 10 个任务） |

---

## 7. 开发注意事项 (Important)
1.  **分页规范**：必须使用 `com.maigen.api.model.dto` 下的 `PageQuery` 和 `PageDTO`。
2.  **DTO 转换**：强制使用 `BeanUtil.copyProperties`。
3.  **事务管理**：涉及积分扣减与流水记录的方法必须添加 `@Transactional`。
4.  **状态码**：
    - `0-待处理`, `1-分析中`, `2-生成中`, `3-验证中`, `4-完成`, `5-失败`, `6-超时`。
    - 统一使用 `TaskStatusEnum` 枚举类。
