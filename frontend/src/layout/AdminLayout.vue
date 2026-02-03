<template>
  <div class="admin-page-wrapper">
    <!-- 极简管理后台顶栏 -->
    <header class="admin-header glass-header">
      <div class="header-left">
        <div class="admin-logo">
          <icon-settings class="text-primary" :style="{ fontSize: '24px' }" />
          <span class="logo-text">MAIGEN Control Center</span>
        </div>
      </div>
      <div class="header-right">
        <a-space size="large">
          <a-button type="text" class="header-btn" @click="goHome">
            <template #icon><icon-dashboard /></template>
            返回前台
          </a-button>
          <a-divider direction="vertical" />
          <div class="admin-user">
            <a-avatar :size="32" class="bg-primary/10">
              <img :src="userStore.userInfo?.avatar || 'https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f1341c7918341.png~tplv-uwbnlip3yd-webp.webp'" />
            </a-avatar>
            <span class="username">{{ userStore.userInfo?.username }}</span>
          </div>
        </a-space>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="admin-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { IconSettings, IconDashboard } from '@arco-design/web-vue/es/icon';

const router = useRouter();
const userStore = useUserStore();

const goHome = () => {
  router.push('/');
};
</script>

<style scoped>
.admin-page-wrapper {
  min-height: 100vh;
  background: var(--mg-bg-1);
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: rgba(var(--mg-bg-2-rgb), 0.7);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  z-index: 100;
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.5px;
  color: var(--mg-text-1);
}

.header-btn {
  color: var(--mg-text-2);
  border-radius: 10px;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 600;
  color: var(--mg-text-1);
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
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
