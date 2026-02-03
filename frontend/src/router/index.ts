import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

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
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('@/views/admin/index.vue'),
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth !== false && !token) {
    next('/login');
  } else {
    next();
  }
});

export default router;
