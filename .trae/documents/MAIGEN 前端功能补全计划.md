# MAIGEN 前端功能补全计划

基于后端已完成的 API 规格，我将分步骤补全前端缺失的核心功能和交互逻辑。

## 1. 认证系统深度补全 (Auth & Security)
- **注册功能集成**：在 [login/index.vue](file:///d:/web/MAIGEN2/frontend/src/views/login/index.vue) 中增加“注册”模式切换。
- **验证码逻辑**：实现 `sendCode` 接口对接，并添加 60s 倒计时 UI 交互。
- **邀请机制**：在注册表单中增加“邀请码”选填项，对接后端的积分奖励逻辑。
- **找回密码**：预留找回密码入口（视情况补全）。

## 2. 个人中心与资料维护 (Profile)
- **新建个人页**：创建 `src/views/user/profile.vue` 并配置路由。
- **资料修改**：对接 `getUserInfo` 和 `updateUserInfo`，支持昵称修改及头像预览切换。
- **密码管理**：对接 `changePassword` 接口，实现基于旧密码校验的修改逻辑。

## 3. 积分中心与激励交互 (Points System)
- **活跃奖励**：在 [dashboard/index.vue](file:///d:/web/MAIGEN2/frontend/src/views/dashboard/index.vue) 增加“每日签到”和“观看广告”按钮，并处理连击/冷却状态。
- **明细查看**：实现一个侧边抽屉或独立页面，展示 [PointsRecordVO](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/model/vo/PointsRecordVO.java) 定义的流水记录。

## 4. 任务历史与容错处理 (Task Center)
- **任务列表页**：创建 `src/views/task/index.vue`，实现全量任务的分页查询与状态过滤。
- **重试机制**：在 [task/detail.vue](file:///d:/web/MAIGEN2/frontend/src/views/task/detail.vue) 中为 `FAILED` 状态的任务增加“重新开始”按钮，调用 `retryTask` 接口。

## 5. 社区互动闭环 (Community)
- **资源解锁**：在 [community/index.vue](file:///d:/web/MAIGEN2/frontend/src/views/community/index.vue) 点击下载时增加积分扣除确认弹窗。
- **发布任务**：实现“分享我的任务”弹窗，将个人生成的题目一键推送到社区。

---

**优先执行步骤**：我将首先从 **1. 认证系统补全** 开始，确保新用户能够正常进入系统。

请确认是否开始执行？