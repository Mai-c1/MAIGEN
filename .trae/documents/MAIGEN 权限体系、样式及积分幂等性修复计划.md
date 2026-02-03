## 1. 社区广场样式修复
- 修改 [community/index.vue](file:///d:/web/MAIGEN2/frontend/src/views/community/index.vue)，将帖子卡片容器的背景和边框强制绑定到 `--mg-bg-card` 和 `--mg-border`。
- 修复资源预览栏在不同主题下的透明度和对比度。

## 2. 权限逻辑重构 (Sa-Token + 前端守卫)
### 后端
- 修改 `UserVO`，添加 `roles` (List<String>) 和 `permissions` (List<String>) 字段。
- 在 `UserServiceImpl.getUserInfo` 中，利用 `StpUtil.getRoleList()` 和 `StpUtil.getPermissionList()` 填充 VO。
### 前端
- 修改 [user.ts](file:///d:/web/MAIGEN2/frontend/src/store/user.ts)，增加 `roles` 和 `permissions` 状态。
- 在 [router/index.ts](file:///d:/web/MAIGEN2/frontend/src/router/index.ts) 的路由守卫中，增加对 `/admin` 路由的拦截，校验用户是否拥有 `admin` 角色。
- 修改 [MainLayout.vue](file:///d:/web/MAIGEN2/frontend/src/layout/MainLayout.vue)，根据权限动态显示“管理后台”入口。

## 3. 管理后台功能补全
### 后端
- 实现 `ManageRoleController`：角色 CRUD、分配权限。
- 实现 `ManagePermissionController`：权限列表查询。
### 前端
- 开发 `RoleManager.vue` 和 `PermissionManager.vue` 组件。
- 更新管理后台侧边栏，增加对应菜单项。

## 4. 积分归还幂等性校验
- 修改 [TaskServiceImpl.java](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/service/impl/TaskServiceImpl.java)：
  - 在 `handleTaskResult` 失败分支中，增加对 `PointsRecord` 的查询。
  - 只有当 `pointsRecordService.exists(taskId, SOURCE_TASK_REFUND)` 为 false 时才执行退款，防止 MQ 重试导致的多退。
