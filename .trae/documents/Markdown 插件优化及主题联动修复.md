## Markdown 与编辑器全局优化方案

### 1. 深度分析与选择
- **Markdown 场景**：统一使用 `md-editor-v3`。理由：支持 LaTeX、Mermaid、主题响应快、Vue3 兼容性好。
- **代码场景**：保留 `ACE-editor`。理由：C++ 代码高亮与缩进处理更专业。
- **弃用方案**：不采用 TinyMCE/WangEditor 等 HTML 型富文本，避免 Markdown 数据转换损耗。

### 2. 实施步骤

#### 第一步：封装主题感知组件 `MgMdEditor`
- 创建 `src/components/MgMdEditor.vue`，实现 `theme` 属性与 `appStore.theme` 的实时联动。
- 解决背景色始终为黑色的 CSS 覆盖问题。

#### 第二步：修复后台管理与预览
- **SystemManager.vue**: 移除硬编码 `theme="dark"`，改为使用新组件。
- **task/detail.vue**: 修复 `MdPreview` 主题，确保亮色模式下背景为浅灰色/白色，文字为黑色。

#### 第三步：题目创建页优化
- **task/create.vue**: 
    - 题目描述：从 `ACE` 切换为 `MgMdEditor`，开启双栏预览。
    - 标准解法：保留 `ACE`，但添加主题监听逻辑，实现 `github` (亮) 与 `tomorrow_night` (暗) 的自动切换。

### 3. 预期效果
- 整个系统的 Markdown 区域（编辑器、预览区）在亮色模式下呈现清晰的白底黑字。
- ACE 代码编辑器不再“一黑到底”，而是随系统主题变换配色。
- 题目编写体验大幅提升（支持实时预览公式和结构）。
