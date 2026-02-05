<template>
  <div class="admin-layout">
    <!-- 左侧迷你导航 -->
    <div class="admin-sidebar glass-card">
      <div class="sidebar-header">
        <icon-settings :style="{ fontSize: '24px' }" />
        <span v-if="!collapsed" class="ml-2 font-bold">管理中枢</span>
      </div>
      <div class="sidebar-menu">
        <div 
          v-for="item in menuItems" 
          :key="item.key"
          class="menu-item"
          :class="{ active: currentRoute.path.startsWith(item.path) }"
          @click="handleNavigate(item.path)"
        >
          <component :is="item.icon" />
          <span v-if="!collapsed" class="ml-3">{{ item.label }}</span>
        </div>
      </div>
      <div class="sidebar-footer" @click="collapsed = !collapsed">
        <icon-menu-unfold v-if="collapsed" />
        <icon-menu-fold v-else />
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="admin-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { 
  IconUser, 
  IconStorage, 
  IconCommand, 
  IconSettings,
  IconMenuFold,
  IconMenuUnfold,
  IconApps,
  IconSafe,
  IconFile
} from '@arco-design/web-vue/es/icon';

const router = useRouter();
const currentRoute = useRoute();
const collapsed = ref(false);

const menuItems = [
  { key: 'users', label: '用户管理', icon: IconUser, path: '/admin/user' },
  { key: 'roles', label: '角色管理', icon: IconSafe, path: '/admin/role' },
  { key: 'permissions', label: '权限管理', icon: IconSafe, path: '/admin/permission' },
  { key: 'workflows', label: '生成方案', icon: IconApps, path: '/admin/workflow' },
  { key: 'content', label: '内容管理', icon: IconStorage, path: '/admin/content' },
  { key: 'tasks', label: '任务监控', icon: IconCommand, path: '/admin/task' },
  { key: 'logs', label: '操作日志', icon: IconFile, path: '/admin/log' },
  { key: 'system', label: '系统设置', icon: IconSettings, path: '/admin/system' },
];

const handleNavigate = (path: string) => {
  router.push(path);
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100%;
  gap: 16px;
  background: transparent;
}

.admin-sidebar {
  width: v-bind("collapsed ? '72px' : '200px'");
  display: flex;
  flex-direction: column;
  padding: 12px 8px;
  transition: all 0.3s cubic-bezier(0.34, 0.69, 0.1, 1);
  flex-shrink: 0;
  border-radius: 20px;
  border: 1px solid var(--mg-border);
  background: var(--mg-bg-card);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--mg-primary);
  overflow: hidden;
}

.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: v-bind("collapsed ? 'center' : 'flex-start'");
  padding: v-bind("collapsed ? '0' : '0 16px'");
  border-radius: 12px;
  cursor: pointer;
  color: var(--mg-text-2);
  transition: all 0.25s;
  white-space: nowrap;
}

.menu-item:hover {
  background: rgba(var(--mg-primary-rgb), 0.08);
  color: var(--mg-primary);
}

.menu-item.active {
  background: linear-gradient(135deg, var(--mg-primary) 0%, #a855f7 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(var(--mg-primary-rgb), 0.4);
}

.sidebar-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--mg-text-3);
  border-top: 1px solid var(--mg-border);
  transition: color 0.2s;
}

.sidebar-footer:hover {
  color: var(--mg-primary);
}

.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: 4px;
  background: transparent;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
