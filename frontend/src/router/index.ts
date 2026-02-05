import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store/user';
import { Message } from '@arco-design/web-vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
      },
      {
        path: 'task/create',
        name: 'TaskCreate',
        component: () => import('@/views/task/create.vue'),
      },
      {
        path: 'task/list',
        name: 'TaskList',
        component: () => import('@/views/task/index.vue'),
      },
      {
        path: 'task/detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/detail.vue'),
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/community/index.vue'),
      },
      {
        path: 'community/detail/:id',
        name: 'CommunityDetail',
        component: () => import('@/views/community/detail.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/profile.vue'),
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { permission: 'admin:dashboard' },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('@/views/admin/index.vue'),
        redirect: '/admin/user', // 默认跳转到用户管理
        children: [
          {
            path: 'user',
            name: 'AdminUser',
            component: () => import('@/views/admin/user/index.vue'),
          },
          {
            path: 'role',
            name: 'AdminRole',
            component: () => import('@/views/admin/role/index.vue'),
          },
          {
            path: 'permission',
            name: 'AdminPermission',
            component: () => import('@/views/admin/permission/index.vue'),
          },
          {
            path: 'workflow',
            name: 'AdminWorkflow',
            component: () => import('@/views/admin/workflow/index.vue'),
          },
          {
            path: 'log',
            name: 'AdminLog',
            component: () => import('@/views/admin/log/index.vue'),
          },
          // 暂时保留其他路径作为占位，后续补充
          { path: 'content', component: () => import('@/views/admin/index.vue') }, 
          { path: 'task', component: () => import('@/views/admin/index.vue') }, 
          { path: 'system', component: () => import('@/views/admin/index.vue') }, 
        ]
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore();
  const token = localStorage.getItem('token');

  // 1. 白名单处理
  if (to.meta.requiresAuth === false) {
    next();
    return;
  }

  // 2. 未登录处理
  if (!token) {
    next('/login');
    return;
  }

  // 3. 已登录但未获取用户信息处理
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo();
    } catch (error) {
      next('/login');
      return;
    }
  }

  // 4. 路由权限校验
  const requiredPermission = to.meta.permission as string;
  if (requiredPermission && !userStore.hasPermission(requiredPermission)) {
    Message.error('您没有访问该页面的权限');
    next('/dashboard');
    return;
  }

  next();
});

export default router;
