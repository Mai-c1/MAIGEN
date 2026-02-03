<template>
  <div class="mg-md-editor-wrapper" :class="theme">
    <MdEditor
      v-model="content"
      :theme="theme"
      :preview="preview"
      :language="language"
      :placeholder="placeholder"
      :toolbars="toolbars"
      :disabled="disabled"
      :readonly="readonly"
      @on-change="handleChange"
      @on-upload-img="handleUploadImg"
      class="mg-md-editor"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { useAppStore } from '@/store/app';

interface Props {
  modelValue: string;
  placeholder?: string;
  preview?: boolean;
  language?: string;
  disabled?: boolean;
  readonly?: boolean;
  toolbars?: any[];
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容...',
  preview: true,
  language: 'zh-CN',
  disabled: false,
  readonly: false,
  toolbars: () => [
    'bold',
    'underline',
    'italic',
    '-',
    'title',
    'strikeThrough',
    'sub',
    'sup',
    'quote',
    'unorderedList',
    'orderedList',
    'task',
    '-',
    'codeRow',
    'code',
    'link',
    'image',
    'table',
    'mermaid',
    'katex',
    '-',
    'revoke',
    'next',
    'save',
    '=',
    'pageFullscreen',
    'fullscreen',
    'preview',
    'htmlPreview',
    'catalog',
  ]
});

const emit = defineEmits(['update:modelValue', 'change', 'upload-img']);

const appStore = useAppStore();
const theme = computed(() => (appStore.theme === 'dark' || appStore.theme === 'midnight') ? 'dark' : 'light');

const content = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const handleChange = (val: string) => {
  emit('change', val);
};

const handleUploadImg = (files: File[], callback: (urls: string[]) => void) => {
  emit('upload-img', files, callback);
};
</script>

<style scoped>
.mg-md-editor-wrapper {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--color-border-2);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.mg-md-editor-wrapper.dark {
  border-color: rgba(255, 255, 255, 0.1);
}

.mg-md-editor {
  height: 100% !important;
}

:deep(.md-editor) {
  --md-bk-color: transparent !important;
}

:deep(.md-editor-dark) {
  --md-bk-color: transparent !important;
}

:deep(.md-editor-content) {
  font-family: inherit;
}
</style>
