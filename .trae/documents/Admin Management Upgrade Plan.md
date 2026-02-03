# 管理后台 (Admin) 架构重构与功能补全计划

## 1. 后端架构重构 (Backend Restructuring)
按照最佳实践，将管理后台接口从单一的 `AdminController` 拆分为模块化的 Controller，并统一放在 `com.maigen.api.controller.admin` 包下。

### 1.1 模块化 Controller 设计
- **`ManageUserController`**: 
  - 核心：用户 (User) 完整 CRUD、状态切换。
  - 扩展：角色分配 (UserRole)、积分调账 (Points adjustment)、邀请关系管理 (Invitation)。
- **`ManageContentController`**: 
  - 核心：社区内容 (CommunityContent) 完整 CRUD、审核流 (Audit)。
  - 扩展：分类 (Category) 管理、标签 (Tag) 管理。
- **`ManageTaskController`**: 
  - 核心：全站任务 (Task) 监控、强制停止、物理/逻辑删除。
  - 扩展：任务策略 (TaskStrategy) 管理。
- **`ManageSystemController`**: 
  - 核心：系统配置 (SystemConfig) 完整 CRUD。
  - 扩展：积分规则 (PointsRule) 管理、操作日志 (OperationLog) 查询。

### 1.2 接口规范
- 每个 Controller 均继承或遵循标准的 CRUD 路径：
  - `GET /admin/[module]` (分页列表，支持多条件过滤)
  - `GET /admin/[module]/{id}` (详情)
  - `POST /admin/[module]` (新增)
  - `PUT /admin/[module]` (全量/增量修改)
  - `DELETE /admin/[module]/{id}` (删除)
- 统一使用 `SaResult` 返回，并在 Controller 级别标注 `@SaCheckRole("管理员")`。

## 2. 前端 UI/UX 深度重设计 (Frontend Redesign)
### 2.1 布局重构 (Sidebar + Content Area)
- **侧边栏导航**: 将现有的顶部 Tabs 替换为左侧侧边栏导航，支持二级菜单，提升扩展性。
  - **用户中心**: 用户列表、角色权限、邀请记录。
  - **内容管理**: 资源审核、全量资源、分类标签。
  - **任务中心**: 任务监控、生成策略。
  - **系统设置**: 参数配置、积分规则、操作审计。

### 2.2 CRUD 交互标准化
- **列表页**: 统一顶部搜索栏 + 底部带分页的 Table。
- **操作反馈**: 使用 `a-drawer` (抽屉) 处理复杂表单，`a-modal` (对话框) 处理简单确认。
- **状态管理**: 引入更直观的状态色块 (Tag) 和进度条。

## 3. 联调与接口补全清单
- **补全 DTO**: 为每个模块创建专门的 `SaveDTO` 和 `UpdateDTO`。
- **接口联调**: 
  - 实现用户新增与密码初始化。
  - 实现内容审核并自动发放奖励。
  - 实现系统参数的动态刷新（关联 Redis 缓存）。
- **权限验证**: 确保所有 `/admin/**` 路径均受到严格的鉴权保护。

## 预期目标
- 消除 `AdminController` 的代码臃肿。
- 实现真正的全模块管理能力，而非仅限于查看。
- 提供符合专业管理系统标准的交互体验。
