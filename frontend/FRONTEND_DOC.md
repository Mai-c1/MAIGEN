# MAIGEN 前端技术规格文档 (FRONTEND_DOC.md)

## 1. 视觉规范
- **主背景色**: `#101014` (Deep Dark)
- **卡片背景色**: `#1D1D21`
- **强调色**: `#165DFF` (Arco Blue)
- **字体**: 系统默认无衬线字体族，代码使用 JetBrains Mono 或 Courier New。

## 2. 核心技术栈
- **Vue 3**: 使用 `<script setup>` 组合式 API。
- **Arco Design Vue**: UI 组件库，默认开启 Dark 模式。
- **Tailwind CSS**: 负责原子化布局与推特风 UI 开发。
- **Ace Editor**: 负责代码编辑与 Markdown 实时预览。
- **ECharts**: 负责仪表盘数据可视化。

## 3. 目录职责
- `src/api`: 定义与后端 `MAIGEN-api` 1:1 对应的请求函数。
- `src/store`: Pinia 存储，主要管理 `user` (用户信息/Token) 和 `app` (主题/侧边栏状态)。
- `src/utils/request.ts`: 基于 Axios 封装，自动处理 Token 注入与 401/429 响应。
- `src/views`: 页面逻辑，按功能模块划分。

## 4. 路由设计 (vue-router)
- `/login`: 登录/注册
- `/dashboard`: 仪表盘首页
- `/task/create`: 创建生成任务
- `/task/list`: 历史任务列表
- `/task/detail/:id`: 任务执行监控（含 Agent 终端）
- `/community`: 广场（推特风）
- `/profile`: 个人设置
- `/admin`: 管理后台（权限校验）

## 5. 状态管理 (Pinia)
- `useUserStore`: 登录、退出、获取用户信息、积分余额同步。
- `useAppStore`: 主题切换 (dark/light)、侧边栏收缩状态。

## 6. 开发建议
- 优先使用 Arco Design 提供的 `Grid` 和 `Space` 进行布局。
- 复杂列表交互使用 Tailwind CSS 微调。
- 所有 API 调用必须经过 `src/api` 统一管理。
