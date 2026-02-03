import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAppStore = defineStore('app', () => {
  const theme = ref(localStorage.getItem('theme') || 'dark');
  const collapsed = ref(false);

  const setTheme = (mode: string) => {
    theme.value = mode;
    localStorage.setItem('theme', mode);
    
    // 清除所有主题类
    const themeClasses = ['theme-light', 'theme-dark'];
    document.body.classList.remove('theme-light', 'theme-dark', 'theme-midnight', 'theme-soft');
    document.body.classList.add(`theme-${mode}`);

    // Arco 适配
    if (mode === 'light') {
      document.body.removeAttribute('arco-theme');
      document.documentElement.classList.remove('dark');
    } else {
      document.body.setAttribute('arco-theme', 'dark');
      document.documentElement.classList.add('dark');
    }
  };

  const setCollapsed = (val: boolean) => {
    collapsed.value = val;
  };

  // 初始化主题
  const initTheme = () => {
    setTheme(theme.value);
  };

  return {
    theme,
    collapsed,
    setTheme,
    setCollapsed,
    initTheme,
  };
});
