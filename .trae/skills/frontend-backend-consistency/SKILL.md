---
name: "frontend-backend-consistency"
description: "确保前端接口、实体类字段与后端保持一致。当修改后端接口、实体类或发现前端调用报错、字段未渲染时调用。"
---

# 前后端一致性审查 (Frontend-Backend Consistency)

本 Skill 用于强制执行前后端字段命名和接口路径的一致性。

## 核心规则

1. **命名规范**: 后端已全面重构为驼峰命名 (camelCase)，前端的所有 TypeScript 接口、对象属性、API 请求参数必须严格使用驼峰命名。
2. **字段对齐**: 前端的接口定义 (Interface/Type) 必须完整覆盖后端 DTO/VO 中的必要字段。
3. **路径同步**: 前端 `src/api/` 下的请求路径必须与后端 Controller 中的 `@RequestMapping` 保持一致。

## 执行步骤

### 1. 字段审查
- 当后端修改了 Entity/DTO/VO 时，检查前端对应的接口定义。
- **禁止使用 `any`**: 鼓励为前端 API 响应和请求参数定义明确的 TypeScript Interface。
- **案例**: 
  - 后端: `private LocalDateTime createdAt;`
  - 前端: `interface Task { createdAt: string; ... }` (正确)
  - 前端: `interface Task { created_at: string; ... }` (错误，需修正)

### 2. 接口审查
- 检查 `frontend/src/api/` 下的函数，确保 URL 路径、请求方法 (GET/POST/PUT/DELETE) 与后端 Controller 匹配。
- 检查路径参数 (Path Variable) 和查询参数 (Query Param) 的变量名。

### 3. 自动化修改
- 如果发现不一致，优先以**后端代码**为准修改前端代码。
- 修改后需检查前端组件 (`views/`, `components/`) 中对该字段的引用，同步进行更名。

## 调用时机
- 修改了后端接口或实体类后。
- 前端页面出现“字段未渲染”或“接口 404/405”报错时。
- 新增功能涉及前后端交互时。
