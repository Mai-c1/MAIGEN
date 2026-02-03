<template>
  <div class="code-editor-container" ref="editorRef"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import ace from 'ace-builds';
import 'ace-builds/src-noconflict/mode-c_cpp';
import 'ace-builds/src-noconflict/mode-markdown';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/theme-github';
import 'ace-builds/src-noconflict/theme-tomorrow_night';
import 'ace-builds/src-noconflict/ext-language_tools';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  mode: {
    type: String,
    default: 'c_cpp',
  },
  theme: {
    type: String,
    default: 'tomorrow_night',
  },
  readonly: {
    type: Boolean,
    default: false,
  },
  autoHeight: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['update:modelValue']);

const editorRef = ref<HTMLElement | null>(null);
let editor: ace.Ace.Editor | null = null;

onMounted(() => {
  if (editorRef.value) {
    editor = ace.edit(editorRef.value, {
      mode: `ace/mode/${props.mode}`,
      theme: `ace/theme/${props.theme}`,
      value: props.modelValue,
      readOnly: props.readonly,
      fontSize: 14,
      tabSize: 2,
      useSoftTabs: true,
      enableBasicAutocompletion: !props.readonly,
      enableLiveAutocompletion: !props.readonly,
      enableSnippets: !props.readonly,
      maxLines: props.autoHeight ? Infinity : undefined,
      minLines: props.autoHeight ? 10 : undefined,
      showPrintMargin: false,
    });

    editor.on('change', () => {
      const value = editor?.getValue();
      if (value !== props.modelValue) {
        emit('update:modelValue', value);
      }
    });
  }
});

watch(() => props.modelValue, (newVal) => {
  if (editor && newVal !== editor.getValue()) {
    editor.setValue(newVal, 1);
  }
});

watch(() => props.mode, (newMode) => {
  if (editor) {
    editor.setOption('mode', `ace/mode/${newMode}`);
  }
});

watch(() => props.theme, (newTheme) => {
  if (editor) {
    editor.setTheme(`ace/theme/${newTheme}`);
  }
});

onBeforeUnmount(() => {
  editor?.destroy();
});
</script>

<style scoped>
.code-editor-container {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  border: 1px solid var(--color-border);
}
</style>
