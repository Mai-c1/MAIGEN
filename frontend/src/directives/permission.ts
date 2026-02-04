import type { Directive } from 'vue';
import { useUserStore } from '../store/user';

/**
 * 权限指令
 * 使用方式：v-permission="'user:delete'" 或 v-permission="['user:delete', 'user:batch-delete']"
 */
export const permission: Directive = {
  mounted(el, binding) {
    const { value } = binding;
    const userStore = useUserStore();

    if (value) {
      const hasPermission = userStore.hasPermission(value);
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el);
      }
    }
  },
  // 当权限动态改变时，也需要重新检查（虽然通常需要重新登录）
  updated(el, binding) {
    const { value } = binding;
    const userStore = useUserStore();

    if (value) {
      const hasPermission = userStore.hasPermission(value);
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el);
      }
    }
  }
};
