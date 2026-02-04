import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { getUserInfo, type UserInfo } from '@/api/user';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null);
  const token = ref(localStorage.getItem('token') || '');
  const permissions = computed(() => userInfo.value?.permissions || []);
  const roles = computed(() => userInfo.value?.roles || []);

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  };

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info;
  };

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfo();
      if (res.code === 200) {
        setUserInfo(res.data);
        return res.data;
      }
      throw new Error(res.message || '获取用户信息失败');
    } catch (error) {
      logout();
      throw error;
    }
  };

  const hasPermission = (perm: string | string[]) => {
    if (!perm) return true;
    const permList = Array.isArray(perm) ? perm : [perm];
    return permissions.value.some((p: string) => permList.includes(p)) || roles.value.includes('管理员');
  };

  const logout = () => {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem('token');
  };

  return {
    userInfo,
    token,
    permissions,
    roles,
    setToken,
    setUserInfo,
    fetchUserInfo,
    hasPermission,
    logout,
  };
});
