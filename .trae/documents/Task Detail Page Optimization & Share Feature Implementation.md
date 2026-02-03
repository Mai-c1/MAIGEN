# 任务详情页动态工作流与 UI 优化计划

## 1. 后端：实现动态阶段列表 (Dynamic Stages)
- **新增 VO**: 创建 [TaskStageVO.java](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/model/vo/TaskStageVO.java)，定义 `name` 和 `state` (0:待处理, 1:进行中, 2:已完成)。
- **VO 增强**: 在 [TaskDetailVO.java](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/model/vo/TaskDetailVO.java) 中增加 `statusDesc` 和 `List<TaskStageVO> stages`。
- **业务实现**: 在 [TaskServiceImpl.java](file:///d:/web/MAIGEN2/backend/MAIGEN-api/src/main/java/com/maigen/api/service/impl/TaskServiceImpl.java) 中动态构造阶段列表，根据当前 `status` 计算各阶段的 `state`。

## 2. 前端：数据驱动的详情页重构
- **动态渲染阶段**: 修改 [detail.vue](file:///d:/web/MAIGEN2/frontend/src/views/task/detail.vue)，使用 `v-for` 循环 `taskInfo.stages` 展示工作流步骤。
- **展示题面信息**: 新增 `a-card` 模块，使用 `pre` 标签渲染 `taskInfo.problemDescription`。
- **UI 瘦身**: 
  - 移除“智能体终端 (Agent Terminal)”卡片。
  - 移除“停止任务”按钮。

## 3. 功能补全：分享与维护
- **分享功能**: 在详情页顶栏增加“分享到社区”按钮（仅成功态可见），并实现分享 Modal。
- **API 修复**: 修正 [community.ts](file:///d:/web/MAIGEN2/frontend/src/api/community.ts) 中 `likeContent` 接口的 URL 模板字符串。
