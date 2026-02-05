<template>
  <div class="task-detail-container">
    <!-- 顶部状态栏 (精简版) -->
    <a-card :bordered="false" class="mb-6 status-header glass-card bg-primary/5 border-primary/10">
      <div class="flex justify-between items-center">
        <div class="flex items-center gap-4">
          <div class="p-3 rounded-2xl bg-primary/10">
            <icon-sync v-if="taskInfo.status < 4" class="text-primary" spin size="24" />
            <icon-check-circle-fill v-else-if="taskInfo.status === 4" class="text-green-500" size="24" />
            <icon-exclamation-circle-fill v-else class="text-red-500" size="24" />
          </div>
          <div>
            <h2 class="text-xl font-bold text-[var(--mg-text-1)] m-0">{{ taskInfo.title || '任务加载中...' }}</h2>
            <div class="flex items-center gap-2 mt-1">
              <a-tag :color="taskInfo.status === 4 ? 'green' : taskInfo.status >= 5 ? 'red' : 'arcoblue'" size="small" class="rounded-md font-bold">
                {{ taskInfo.statusDesc || '处理中' }}
              </a-tag>
            </div>
          </div>
        </div>
        <a-space size="medium">
          <a-button v-if="taskInfo.status === 4" type="outline" status="success" @click="openShareModal" class="rounded-xl">
            <template #icon><icon-share-alt /></template>
            分享到社区
          </a-button>
          <a-button type="primary" status="danger" @click="handleRetry" class="rounded-xl px-6">
            <template #icon><icon-refresh /></template>
            {{ taskInfo.status === 4 ? '重新生成' : '重试任务' }}
          </a-button>
          <a-button type="primary" :disabled="taskInfo.status !== 4" @click="handleDownload" class="rounded-xl px-6">
            <template #icon><icon-download /></template>
            下载测试数据
          </a-button>
        </a-space>
      </div>
    </a-card>

    <a-row :gutter="20">
      <a-col :span="14">
        <!-- 题目需求 -->
        <a-card title="题目需求 (Problem)" :bordered="false" class="mb-6 glass-card">
          <div class="markdown-wrapper rounded-2xl">
            <MdPreview 
              editor-id="task-problem-preview"
              :modelValue="cleanedDescription" 
              :theme="appStore.theme"
              preview-theme="github"
              code-theme="atom"
            />
          </div>
        </a-card>

        <!-- 代码参考 -->
        <a-card title="参考代码 (Reference)" :bordered="false" class="glass-card">
          <template #extra>
            <a-tag color="green" size="small">C++ 17</a-tag>
          </template>
          <div class="rounded-2xl overflow-hidden border border-[var(--mg-border)]">
            <CodeEditor
              :model-value="taskInfo.standardCode"
              mode="c_cpp"
              readonly
              :theme="aceTheme"
              auto-height
            />
          </div>
        </a-card>
      </a-col>

      <a-col :span="10">
        <!-- 执行终端 (Terminal) -->
        <a-card title="执行控制台 (Console)" :bordered="false" class="mb-6 glass-card terminal-card">
          <div class="terminal-content p-4 font-mono text-sm leading-relaxed overflow-y-auto h-[350px]" style="background: var(--mg-terminal-bg);">
            <div v-for="(log, index) in logs" :key="index" class="mb-2 flex gap-3">
              <span class="text-[var(--mg-text-3)] shrink-0">[{{ log.time }}]</span>
              <span :class="{
                'text-blue-400': log.type === 'SYSTEM',
                'text-purple-400': log.type === 'AGENT',
                'text-yellow-400': log.type === 'SANDBOX',
                'text-red-400': log.type === 'ERROR'
              }" class="font-bold shrink-0">[{{ log.type }}]</span>
              <span class="text-[var(--mg-text-2)]">{{ log.content }}</span>
            </div>
            <div v-if="taskInfo.status < 4" class="flex items-center gap-2 text-blue-400 animate-pulse">
              <icon-sync spin />
              <span>智能体正在努力工作中...</span>
            </div>
          </div>
        </a-card>

        <!-- 任务进度 (Progress) -->
        <a-card title="生成进度 (Progress)" :bordered="false" class="mb-6 glass-card overflow-hidden">
          <div class="p-4">
            <div class="flex justify-between items-end mb-4">
              <div>
                <div class="text-[var(--mg-text-3)] text-xs font-bold uppercase tracking-wider mb-1">当前阶段</div>
                <div class="text-[var(--mg-text-1)] font-black text-lg flex items-center gap-2">
                  <icon-loading v-if="taskInfo.status < 4" spin />
                  {{ taskInfo.statusDesc || '等待中' }}
                </div>
              </div>
              <div class="text-right">
                <span class="text-3xl font-black text-primary">{{ taskInfo.progress || 0 }}</span>
                <span class="text-primary/60 font-bold ml-1">%</span>
              </div>
            </div>
            <a-progress 
              :percent="(taskInfo.progress || 0) / 100" 
              :show-text="false" 
              :status="taskInfo.status >= 5 ? 'danger' : taskInfo.status === 4 ? 'success' : 'normal'"
              animation
              class="custom-progress-bar"
            />
            <div class="mt-4 flex items-center gap-2 p-3 rounded-xl bg-[var(--mg-bg-1)] border border-[var(--mg-border)]">
              <icon-info-circle class="text-primary/60" />
              <span class="text-[var(--mg-text-3)] text-xs">
                {{ taskInfo.status < 4 ? '任务正在后台安全沙箱中执行，请稍候...' : '任务已执行完毕，点击右上方按钮可下载结果。' }}
              </span>
            </div>
          </div>
        </a-card>

        <!-- 错误诊断 (仅失败时显示) -->
        <transition name="fade">
          <a-card v-if="taskInfo.errorMessage" title="错误诊断 (Diagnostics)" :bordered="false" class="mb-6 glass-card border-red-500/30 bg-red-500/5">
            <div class="p-4 bg-red-500/10 rounded-xl border border-red-500/20">
              <div class="flex items-center gap-2 text-red-500 font-bold mb-2">
                <icon-exclamation-circle-fill /> 检测到执行异常
              </div>
              <p class="text-red-400 text-sm m-0 leading-relaxed font-mono">
                {{ taskInfo.errorMessage }}
              </p>
            </div>
          </a-card>
        </transition>

        <!-- 任务参数 -->
        <a-card title="配置详情 (Config)" :bordered="false" class="glass-card">
          <a-descriptions :column="1" size="small" class="custom-descriptions">
            <a-descriptions-item label="任务 ID">
              <span class="font-mono opacity-70">{{ taskInfo.id }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="测试点数量">{{ taskInfo.testcaseCount }} 个</a-descriptions-item>
            <a-descriptions-item label="时空限制">
              <a-space>
                <a-tag size="small" class="rounded-md">{{ taskInfo.timeLimit }}ms</a-tag>
                <a-tag size="small" class="rounded-md">{{ taskInfo.memoryLimit }}MB</a-tag>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="生成方案">
              <a-tag color="arcoblue" size="small" class="rounded-md">
                <template #icon><icon-apps /></template>
                {{ taskInfo.workflowName || '标准Python生成' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="创建于">
              <span class="opacity-70">{{ formatTime(taskInfo.createdAt) }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 分享弹窗 -->
    <a-modal v-model:visible="shareVisible" title="发布到社区" @before-ok="handleShare" class="glass-modal">
      <a-form :model="shareForm" layout="vertical" class="mt-2">
        <a-form-item label="资源标题" required>
          <a-input v-model="shareForm.title" placeholder="给你的资源起个吸引人的名字" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
        </a-form-item>
        <a-form-item label="分类" required>
          <a-select v-model="shareForm.categoryId" placeholder="选择所属分类" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]">
            <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="解锁所需积分" required>
          <a-input-number v-model="shareForm.points" :min="0" :max="100" placeholder="0-100" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
        </a-form-item>
        <a-form-item label="标签">
          <a-input-tag v-model="shareForm.tags" placeholder="输入标签按回车添加" allow-clear class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { MdPreview, config } from 'md-editor-v3';
import 'md-editor-v3/lib/preview.css';

// 允许 md-editor-v3 使用 CDN 加载依赖（如 katex）
// 移动到组件外部，确保全局只配置一次
config({
  editorConfig: {
    renderConfig: {
      mermaid: {
        useJs: true
      }
    }
  }
});

import { 
  IconSync, 
  IconCheckCircleFill, 
  IconRefresh,
  IconShareAlt,
  IconClockCircle,
  IconDownload,
  IconExclamationCircleFill,
  IconInfoCircle,
  IconLoading,
  IconApps
} from '@arco-design/web-vue/es/icon';
import CodeEditor from '@/components/CodeEditor.vue';
import { getTaskDetail, retryTask, getTaskLogs } from '@/api/task';
import { shareContent, getCategories } from '@/api/community';
import { useAppStore } from '@/store/app';

const route = useRoute();
const appStore = useAppStore();
const aceTheme = computed(() => appStore.theme === 'dark' || appStore.theme === 'midnight' ? 'tomorrow_night' : 'github');
const taskInfo = ref<any>({
  progress: 0,
  status: 0,
  problemDescription: '',
  strategies: []
});

// 模拟终端日志
const logs = ref<{ time: string; type: string; content: string }[]>([]);

const addLog = (type: string, content: string) => {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false });
  // 去重：避免重复添加相同的日志
  const exists = logs.value.some(l => l.content === content && l.type === type);
  if (!exists) {
    logs.value.push({ time, type, content });
    // 保持最新日志可见
    nextTick(() => {
      const el = document.querySelector('.terminal-content');
      if (el) el.scrollTop = el.scrollHeight;
    });
  }
};

// 获取真实日志
const fetchLogs = async () => {
  try {
    const res: any = await getTaskLogs(route.params.id as string);
    if (res && res.data) {
      // 合并日志，避免重复
      res.data.forEach((log: any) => {
        const content = `执行步骤 [${log.stepOrder}]: ${log.roleName}`;
        addLog('AGENT', content);
        // 如果有 Prompt 快照或 AI 响应，也可以考虑展示（这里先只展示摘要）
      });
    }
  } catch (error) {
    console.error('Fetch logs failed:', error);
  }
};

const statusLogMap: Record<number, { type: string; content: string }> = {
  0: { type: 'SYSTEM', content: '任务已进入排队序列，等待分配计算资源...' },
  1: { type: 'AGENT', content: 'AI 智能体已接管，正在深度解析题目需求与约束...' },
  2: { type: 'AGENT', content: '分析完成，正在基于策略生成高性能测试点代码...' },
  3: { type: 'SANDBOX', content: '代码生成完毕，正在拉取隔离沙箱环境进行自动化验证...' },
  4: { type: 'SYSTEM', content: '验证通过！测试数据包已打包完成，随时可下载。' },
  5: { type: 'ERROR', content: '执行过程中遇到致命错误，请检查题目或重试。' },
  6: { type: 'ERROR', content: '任务执行时长超过预设阈值，已被系统强制中止。' },
  7: { type: 'SYSTEM', content: '任务已被用户手动取消。' },
};

// 处理 Markdown 中的换行符，确保在不同系统下显示正常
const cleanedDescription = computed(() => {
  let desc = taskInfo.value.problemDescription || '';
  // 如果是后端返回的带有字面量 \n 的字符串，进行转换
  if (desc.includes('\\n')) {
    desc = desc.replace(/\\n/g, '\n');
  }
  if (desc.includes('\\r')) {
    desc = desc.replace(/\\r/g, '');
  }
  return desc;
});

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return '-';
  try {
    const date = new Date(timeStr);
    return date.toLocaleString();
  } catch (e) {
    return timeStr;
  }
};

// 分享相关
const shareVisible = ref(false);
const categories = ref<any[]>([]);
const shareForm = ref({
  title: '',
  categoryId: '',
  points: 10,
  tags: [],
  taskId: ''
});

const openShareModal = async () => {
  shareForm.value.title = taskInfo.value.title;
  shareForm.value.taskId = taskInfo.value.id;
  shareVisible.value = true;
  
  if (categories.value.length === 0) {
    try {
      const res: any = await getCategories();
      categories.value = res.data || [];
    } catch (error) {}
  }
};

const handleShare = async () => {
  if (!shareForm.value.title || !shareForm.value.categoryId) {
    Message.warning('请填写完整信息');
    return false;
  }
  try {
    await shareContent({
      ...shareForm.value,
      problemDescription: taskInfo.value.problemDescription,
      standardCode: taskInfo.value.standardCode
    });
    Message.success('分享成功！已发布到社区广场');
    return true;
  } catch (error) {
    return false;
  }
};

let timer: any = null;

const fetchStatus = async () => {
  try {
    const res: any = await getTaskDetail(route.params.id as string);
    if (res && res.data) {
      const oldStatus = taskInfo.value.status;
      const newStatus = res.data.status;
      
      // 优化：仅在状态或进度发生变化时才更新，减少重新渲染
      if (newStatus !== taskInfo.value.status || 
          res.data.progress !== taskInfo.value.progress || 
          res.data.id !== taskInfo.value.id) {
        taskInfo.value = res.data;
        
        // 如果状态变化，添加日志
        if (newStatus !== oldStatus) {
          const logInfo = statusLogMap[newStatus];
          if (logInfo) {
            addLog(logInfo.type, logInfo.content);
          }
          if (res.data.errorMessage) {
            addLog('ERROR', `错误详情: ${res.data.errorMessage}`);
          }
        }
      }
      
      // 再次检查是否达到终态并清除定时器
      if ([4, 5, 6, 7].includes(newStatus)) {
        if (timer) {
          clearInterval(timer);
          timer = null;
          console.log('Task reached final state, polling stopped.');
        }
        // 任务完成后，额外拉取一次完整日志
        fetchLogs();
      } else {
        // 进行中时，尝试拉取增量日志
        fetchLogs();
      }
    }
  } catch (error) {
    console.error('Fetch task detail failed:', error);
  }
};

const handleRetry = async () => {
  try {
    const res = await retryTask(route.params.id as string);
    Message.success(res.msg || '任务已重启');
    
    // 重置本地状态，立即反馈
    taskInfo.value.status = 0;
    taskInfo.value.progress = 0;
    logs.value = []; // 清空日志
    addLog('SYSTEM', '用户手动发起重试，正在重新初始化...');
    
    // 立即执行一次 fetch，虽然可能 backend 还没更新，但为了保险
    fetchStatus();
    
    // 重新开启定时器
    if (timer) clearInterval(timer);
    timer = setInterval(fetchStatus, 3000);
  } catch (error) {
    console.error(error);
  }
};

const handleDownload = () => {
  if (taskInfo.value.resultUrl) {
    window.open(taskInfo.value.resultUrl);
  } else {
    Message.warning('暂无下载链接');
  }
};

onMounted(async () => {
  await fetchStatus();
  // 初始日志
  if (logs.value.length === 0) {
    const logInfo = statusLogMap[taskInfo.value.status];
    if (logInfo) addLog(logInfo.type, logInfo.content);
  }
  
  // 初始启动定时器逻辑（仅在非终态时启动）
  if (![4, 5, 6, 7].includes(taskInfo.value.status)) {
    timer = setInterval(async () => {
      await fetchStatus();
    }, 3000);
  }
});

onBeforeUnmount(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
.task-detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.custom-progress :deep(.arco-progress-line-bar) {
  height: 8px !important;
  border-radius: 4px;
}

.custom-progress-bar :deep(.arco-progress-line-bar) {
  height: 12px !important;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--mg-primary), #a855f7);
}

.custom-progress-bar :deep(.arco-progress-line-inner) {
  background: var(--mg-bg-1);
  border: 1px solid var(--mg-border);
}

.custom-descriptions :deep(.arco-descriptions-item-label) {
  color: var(--mg-text-3);
  font-weight: 500;
}

.custom-descriptions :deep(.arco-descriptions-item-value) {
  color: var(--mg-text-1);
  font-weight: 600;
}

.markdown-wrapper :deep(.md-editor-preview-wrapper) {
  padding: 24px;
  background: transparent !important;
}

.markdown-wrapper :deep(.md-editor-preview) {
  color: var(--mg-text-2);
  font-size: 15px;
  line-height: 1.8;
}

/* 终端滚动条美化 */
.terminal-content::-webkit-scrollbar {
  width: 6px;
}

.terminal-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.terminal-content::-webkit-scrollbar-track {
  background: transparent;
}

.terminal-card {
  overflow: hidden;
}

.terminal-content {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
}

/* 修复数学公式换行问题 */
.markdown-wrapper :deep(.katex-display) {
  margin: 1em 0;
  overflow-x: auto;
  overflow-y: hidden;
}

:deep(.arco-card-header) {
  border-bottom: 1px solid var(--mg-border);
  padding: 16px 20px;
}

:deep(.arco-card-header-title) {
  font-weight: 800;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--mg-text-2);
}
</style>
