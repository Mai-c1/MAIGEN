# MAIGEN (Multi-Agent Intelligent Generation)

MAIGEN 是一个专为 OI/ICPC 参赛者和出题人设计的自动化数据生成平台。它利用无状态计算、编排器模式和多智能体协同技术，能够快速生成高强度、边界覆盖全的测试数据。

## 核心亮点

*   **可编排 AI 工作流 (Orchestrated AI Workflow)**: 
    *   不仅仅是固定的多 Agent 协作，MAIGEN 支持通过管理后台**动态配置 AI 工作流**。
    *   您可以自定义任意数量的 Agent 步骤（如：题目分析 -> 策略制定 -> 代码生成 -> 代码审计 -> 自动修正）。
    *   内置**自动反馈闭环 (Auto-Feedback Loop)**：当代码审计未通过时，系统会自动触发回滚和重试，携带审计意见指导 AI 修正代码。
*   **高性能沙箱执行**: 
    *   集成 `go-judge` 高性能评测沙箱。
    *   支持**文件挂载与流式传输**，避免 Base64 编解码开销，实现大规模测试数据（GB 级）的秒级打包与上传。
    *   支持动态配置沙箱执行脚本 (`run.sh` 模板)，灵活适配不同语言和生成逻辑。
*   **全异步架构**: 
    *   基于 RabbitMQ 的事件驱动架构，任务提交、AI 分析、代码执行、结果回传全流程异步化，支持高并发任务处理。

## 核心功能

*   **用户与权限**: 
    *   完善的注册（邮箱验证）、登录流程。
    *   基于 **Sa-Token** 的 RBAC 权限体系，支持细粒度的接口权限控制和自定义角色。
*   **任务配置**: 
    *   支持 C++ 标准程序上传（Ace Editor 高亮支持）。
    *   支持 Markdown 格式题面预览。
    *   灵活的任务参数设置（时间限制、内存限制、测试点数量）。
*   **管理后台 (Admin)**:
    *   **AI 工作流管理**: 可视化编辑 AI 步骤、Prompt 模板、角色定义。
    *   **系统监控**: 查看任务执行日志、系统资源状态。
    *   **用户与内容管理**: 统一管理用户、角色、权限及社区内容。
*   **社区分享**: 用户可分享优质题目与数据，支持积分兑换与下载。

## 技术栈

### 后端 (Backend)
*   **开发语言**: Java 17+
*   **核心框架**: Spring Boot 3.x
*   **数据库**: MySQL 8.0+ (MyBatis-Plus)
*   **缓存**: Redis 7.0+
*   **消息队列**: RabbitMQ 3.10+
*   **认证鉴权**: Sa-Token
*   **对象存储**: MinIO / 阿里云 OSS
*   **判题沙箱**: go-judge (Executorserver)
*   **AI 客户端**: Spring AI (对接 OpenAI/Qwen 等模型)

### 前端 (Frontend)
*   **框架**: Vue 3 + TypeScript
*   **构建工具**: Vite
*   **UI 组件库**: **Arco Design Vue**
*   **样式工具**: Tailwind CSS, Sass
*   **状态管理**: Pinia
*   **编辑器**: Ace Editor, MD Editor V3
*   **图表**: ECharts
*   **图标**: IconPark, Lucide

### 部署与运维
*   **容器化**: Docker & Docker Compose
*   **网关**: Nginx

## 目录结构说明

```text
MAIGEN2/
├── backend/                # 后端项目源码
│   ├── MAIGEN-api/         # 核心 API 服务 (业务逻辑、用户交互、管理后台)
│   ├── MAIGEN-analysis/    # AI 分析服务 (执行动态编排的 AI 工作流)
│   ├── MAIGEN-sandbox/     # 沙箱执行服务 (对接 go-judge，处理编译运行)
│   ├── MAIGEN-common/      # 公共模块 (工具类、常量、DTO)
│   └── docs/               # 后端相关文档
├── frontend/               # 前端项目源码 (Vue 3 + Arco Design)
├── deploy/                 # 部署配置文件
│   ├── mysql/              # MySQL 配置与初始化脚本
│   ├── redis/              # Redis 配置
│   ├── rabbitmq/           # RabbitMQ 配置
│   ├── nginx/              # Nginx 配置
│   └── docker-compose.yml  # 容器编排文件
├── docs/                   # 项目核心文档
│   ├── 需求文档.md          # 产品需求文档 (PRD)
│   └── 数据库设计方案.md     # 详细数据库表结构设计
└── README.md               # 项目说明文件
```

## 快速开始

### 1. 环境准备
*   Docker Desktop 或 Docker Engine
*   JDK 17+
*   Node.js 18+
*   Maven 3.8+

### 2. 启动基础服务
使用 Docker Compose 启动 MySQL, Redis, RabbitMQ, MinIO, go-judge 等基础设施：

```bash
cd deploy
docker-compose up -d
```
> 注意：首次启动会自动执行 `deploy/mysql/init-scripts` 下的 SQL 脚本初始化数据库。

### 3. 后端启动
1.  **配置修改**: 检查 `backend` 下各模块的 `application.yml`，确保数据库、Redis、RabbitMQ 连接地址与本地环境一致。
2.  **编译安装公共模块**:
    ```bash
    cd backend
    mvn clean install -DskipTests
    ```
3.  **启动服务**:
    *   启动 `MAIGEN-api` (主服务)
    *   启动 `MAIGEN-analysis` (AI 服务)
    *   启动 `MAIGEN-sandbox` (沙箱服务)

### 4. 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 5. 访问系统
*   前端页面: `http://localhost:5173` (视 Vite 配置而定)
*   后端 API: `http://localhost:8080`
*   Go-Judge: `http://localhost:5050` (如果在 Docker 中启动)

## 文档资源

*   [产品需求文档 (PRD)](docs/需求文档.md)
*   [数据库设计方案](docs/数据库设计方案.md)
*   [前端架构文档](FRONTEND_ARCHITECTURE.md)
*   [MAIGEN-api 模块说明](backend/MAIGEN-api/README.md)
*   [MAIGEN-analysis 模块说明](backend/MAIGEN-analysis/README.md)
*   [MAIGEN-sandbox 技术规范](backend/MAIGEN-sandbox/README.md)

## 贡献指南

欢迎提交 Issue 和 Pull Request。在提交代码前，请确保通过了所有测试并符合项目的代码规范。
