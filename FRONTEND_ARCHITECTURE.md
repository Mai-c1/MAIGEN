# MAIGEN Frontend Architecture

## 1. Project Overview
- **Name**: maigen-frontend
- **Type**: Vue 3 Single Page Application (SPA)
- **Goal**: Provide a modern, dark-themed UI for the MAIGEN multi-agent algorithm contest platform.

## 2. Technology Stack
- **Core Framework**: Vue 3.4+ (Script Setup)
- **Build Tool**: Vite 5+
- **Language**: TypeScript 5+
- **UI Framework**: Arco Design Vue (Dark mode optimized)
- **Styling**: Tailwind CSS (Utility-first) + CSS Variables
- **State Management**: Pinia (User preferences, Auth state)
- **Routing**: Vue Router 4
- **HTTP Client**: Axios (Interceptors for Sa-Token)
- **Specialized Components**:
    - **Ace Editor**: For code input (C++, Markdown).
    - **ECharts**: For dashboard visualization.

## 3. Directory Structure
```
src/
├── api/             # API definition (Task, User, Community)
├── assets/          # Static assets (Images, Global CSS)
├── components/      # Shared components (CodeEditor, StatsCard)
├── hooks/           # Composable logic (useTheme, useAuth)
├── layout/          # Layout wrappers (MainLayout, AuthLayout)
├── router/          # Route definitions
├── store/           # Pinia stores (user, app)
├── types/           # TS interfaces
├── utils/           # Helpers (request, format)
└── views/           # Page views
    ├── dashboard/   # Dashboard with charts
    ├── task/        # Task creation & details
    ├── community/   # Twitter-style feed
    └── login/       # Auth pages
```

## 4. UI/UX Guidelines
- **Theme**: Deep Dark Mode (#101014 Background, #165DFF Accent).
- **Layout**: Sidebar navigation + Top header + Content area.
- **Font**: Inter, Roboto, or System UI.

## 5. Key Features Implementation
- **Dashboard**: Grid layout with stats cards and ECharts.
- **Task Workflow**: Real-time progress bar for Agent status.
- **Community**: Infinite scroll feed with "Like/Rate/Download" actions.
- **Auth**: JWT token storage in localStorage + Axios interceptor injection.
