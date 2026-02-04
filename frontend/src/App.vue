<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useAppStore } from '@/store/app';

const appStore = useAppStore();

onMounted(() => {
  appStore.initTheme();
});
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
}
</style>
