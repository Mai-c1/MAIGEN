# MAIGEN-api 核心功能扩展实施计划 (确认版)

根据您的指示“开始吧”，我将立即按以下步骤执行功能开发：

## 1. 数据库初始化更新 (init.sql)
我将更新 `docs/init.sql`，增加以下结构：
- **`task` 表**: 增加 `retry_count` (重试次数) 和 `error_message` (失败原因)。
- **`community_content` 表**: 增加 `category_id` (分类ID)、`like_count` (点赞数)、`rating_avg` (平均分)、`rating_count` (评分人数)。
- **新增表**:
    - `user_sign_in`: 记录每日签到。
    - `category` & `tag` & `community_tag`: 分类与标签体系。
    - `community_like` & `community_rating`: 互动评价体系。
    - `operation_log`: 安全审计日志。

## 2. 核心功能分步实现
### **第一步：任务系统优化**
- 实现 `POST /task/retry/{taskId}` 接口。
- 在 `handleTaskResult` 中持久化错误详情。

### **第二步：积分激励体系**
- 实现 `POST /points/sign-in` (每日签到)。
- 实现 `POST /points/ad-reward` (广告模拟激励)。
- 更新 `PointsConstants` 增加相关奖励配置。

### **第三步：社区互动系统**
- 实现点赞 (`/like`) 与 评分 (`/rate`) 接口。
- 实现分类与标签的筛选逻辑。

### **第四步：安全与运维**
- 引入接口限流机制 (Redis)。
- 实现操作日志拦截器 (AOP)。

**我现在将立即开始执行第一步：更新数据库脚本及任务重试功能。**
