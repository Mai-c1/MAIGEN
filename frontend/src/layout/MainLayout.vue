<template>
  <a-layout class="layout-container">
    <a-layout-sider
      breakpoint="lg"
      :width="280"
      :collapsed-width="88"
      collapsible
      :collapsed="collapsed"
      @collapse="onCollapse"
      class="sidebar glass-card"
    >
      <div class="logo">
        <div class="logo-box">
          <!-- 这里预留图片 LOGO 位置 -->
          <div class="logo-image-placeholder">
            <icon-code-square v-if="!collapsed" :style="{ fontSize: '24px', color: '#fff' }" />
            <icon-code-square v-else :style="{ fontSize: '32px', color: '#fff' }" />
          </div>
          <span v-if="!collapsed" class="logo-text">MAIGEN</span>
        </div>
      </div>
      <a-menu
        :selected-keys="selectedKeys"
        :style="{ width: '100%', background: 'transparent' }"
        @menu-item-click="handleMenuClick"
        class="custom-menu"
        :collapsed="collapsed"
      >
        <a-menu-item key="Dashboard">
          <template #icon><icon-dashboard /></template>
          仪表盘
        </a-menu-item>
        <a-menu-item key="TaskCreate">
          <template #icon><icon-plus-circle /></template>
          创建任务
        </a-menu-item>
        <a-menu-item key="Community">
          <template #icon><icon-common /></template>
          社区广场
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout class="main-layout">
      <a-layout-header class="header glass-header">
        <div class="header-left">
          <!-- 移除搜索框 -->
        </div>
        <div class="header-right">
          <a-space size="large">
            <!-- 主题切换器 -->
            <a-button type="text" class="header-btn" @click="appStore.toggleTheme">
              <template #icon>
                <icon-moon-fill v-if="appStore.theme === 'dark'" />
                <icon-sun-fill v-else />
              </template>
            </a-button>

            <div class="points-badge">
              <icon-safe />
              <span>{{ pointsBalance.toLocaleString() }} MAI</span>
            </div>
            
            <!-- 移除消息图标 -->

            <a-dropdown trigger="click">
              <a-avatar :size="36" class="cursor-pointer border-2 border-primary transition-transform hover:scale-105">
                <img 
                  :key="userStore.userInfo?.avatar"
                  :src="userStore.userInfo?.avatar"
                />
              </a-avatar>
              <template #content>
                <a-doption @click="router.push('/profile')">
                  <template #icon><icon-user /></template>
                  个人中心
                </a-doption>
                <a-doption @click="router.push('/profile')">
                  <template #icon><icon-safe /></template>
                  安全设置
                </a-doption>
                <a-divider v-if="isAdmin" style="margin: 4px 0" />
                <a-doption v-permission="`admin:dashboard`" @click="router.push('/admin')">
                  <template #icon><icon-settings /></template>
                  管理后台
                </a-doption>
                <a-divider style="margin: 4px 0" />
                <a-doption @click="handleLogout">
                  <template #icon><icon-export /></template>
                  退出登录
                </a-doption>
              </template>
            </a-dropdown>
          </a-space>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <div class="page-container">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { useAppStore } from '@/store/app';
import { getPointsBalance } from '@/api/points';
import {
  IconDashboard,
  IconPlusCircle,
  IconCommon,
  IconSettings,
  IconMoonFill,
  IconSunFill,
  IconSafe,
  IconNotification,
  IconUser,
  IconExport,
  IconSkin,
  IconCodeSquare,
  IconCloud,
  IconThunderbolt
} from '@arco-design/web-vue/es/icon';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

const collapsed = computed(() => appStore.collapsed);
const pointsBalance = ref(0);
const selectedKeys = computed(() => route.name ? [route.name as string] : []);
const isAdmin = computed(() => userStore.userInfo?.role === 'admin');

const modes = [
  { id: 'light', name: '明亮模式', icon: IconSunFill },
  { id: 'dark', name: '暗黑模式', icon: IconMoonFill },
];

const fetchPoints = async () => {
  try {
    const res: any = await getPointsBalance();
    // 修复积分余额显示 object 的问题
    const balance = typeof res.data === 'object' ? res.data.balance : res.data;
    pointsBalance.value = balance || 0;
  } catch (error) {}
};

onMounted(() => {
  appStore.initTheme();
  fetchPoints();
});

const onCollapse = (val: boolean) => {
  appStore.setCollapsed(val);
};

const handleMenuClick = (key: string) => {
  router.push({ name: key });
};

const handleLogout = () => {
  userStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: var(--mg-bg-1);
}

.sidebar {
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  z-index: 100;
  margin: 12px;
  height: calc(100vh - 24px);
  border-radius: 24px !important;
  transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
  overflow: hidden;
}

.sidebar :deep(.arco-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

.main-layout {
  background: transparent;
  transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
}

.logo {
  height: 100px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.logo-image-placeholder {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--mg-primary) 0%, #a855f7 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(var(--mg-primary-rgb), 0.5);
  transition: all 0.3s ease;
}

.logo-image-placeholder:hover {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 0 6px 25px rgba(var(--mg-primary-rgb), 0.6);
}

.logo-text {
  color: var(--mg-text-1);
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 1px;
  white-space: nowrap;
  text-shadow: 0 0 10px rgba(var(--mg-primary-rgb), 0.2);
}

/* 收起状态的 Logo 样式优化 */
:deep(.arco-layout-sider-collapsed) .logo {
  padding: 0;
  justify-content: center;
  margin-left: 0;
  margin-right: 0;
}

:deep(.arco-layout-sider-collapsed) .logo-box {
  width: 52px;
  height: 52px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
}

:deep(.arco-layout-sider-collapsed) .logo-image-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  margin: 0 auto;
}

.custom-menu {
  padding: 0 12px;
}

:deep(.arco-menu-collapsed) {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 !important;
}

:deep(.arco-menu-inner) {
  padding: 4px 0;
}

:deep(.arco-menu-item) {
  border-radius: 12px;
  margin-bottom: 8px;
  height: 48px;
  line-height: 48px;
  transition: all 0.2s;
}

/* 修复收起状态下的菜单项 */
:deep(.arco-menu-collapsed) .arco-menu-item {
  width: 48px !important;
  height: 48px !important;
  padding: 0 !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:deep(.arco-menu-collapsed) .arco-menu-icon {
  margin-right: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:deep(.arco-menu-collapsed) .arco-icon {
  font-size: 22px !important;
}

:deep(.arco-menu-selected) {
  background-color: var(--mg-primary) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(var(--mg-primary-rgb), 0.3);
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  margin: 12px 24px 0 12px;
  border-radius: 20px;
}

.header-search :deep(.arco-input-wrapper) {
  background: var(--mg-bg-1);
  border-radius: 12px;
}

.header-btn {
  font-size: 18px;
  color: var(--mg-text-2);
  border-radius: 10px;
}

.points-badge {
  background: rgba(var(--mg-primary-rgb), 0.1);
  color: var(--mg-primary);
  padding: 6px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  border: 1px solid rgba(var(--mg-primary-rgb), 0.2);
  box-shadow: 0 0 15px rgba(var(--mg-primary-rgb), 0.1);
  transition: all 0.3s ease;
}

.points-badge:hover {
  background: rgba(var(--mg-primary-rgb), 0.15);
  box-shadow: 0 0 20px rgba(var(--mg-primary-rgb), 0.2);
  transform: translateY(-1px);
}

.header-icon {
  font-size: 22px;
  cursor: pointer;
  color: var(--mg-text-2);
}

.content {
  padding: 24px;
  overflow-y: auto;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

.theme-panel {
  padding: 16px;
  width: 200px;
}

.theme-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--mg-text-1);
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.theme-item {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.theme-item:hover {
  transform: scale(1.2);
}

.theme-item.active {
  border-color: var(--mg-text-1);
  transform: scale(1.1);
}

/* Page Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
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
