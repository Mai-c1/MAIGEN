<template>
  <div class="task-list-container p-6">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-3xl font-black mg-text-gradient m-0 mb-2">生成任务</h1>
        <p class="text-[var(--mg-text-3)] text-sm">管理您的 AI 数据生成任务及其进度</p>
      </div>
      <a-button type="primary" size="large" @click="router.push('/task/create')" class="rounded-xl shadow-lg shadow-primary/20">
        <template #icon><icon-plus /></template>
        新建生成任务
      </a-button>
    </div>

    <a-card :bordered="false" class="glass-card rounded-2xl overflow-hidden">
      <a-table :data="tasks" :loading="loading" :pagination="pagination" @page-change="handlePageChange" :bordered="false" class="custom-table">
        <template #columns>
          <a-table-column title="任务标题" data-index="title">
            <template #cell="{ record }">
              <span 
                class="font-bold text-[var(--mg-text-1)] cursor-pointer hover:text-primary transition-colors"
                @click="router.push(`/task/detail/${record.taskId}`)"
              >
                {{ record.title }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="status">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.status)" class="rounded-lg px-3">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="生成进度" data-index="progress">
            <template #cell="{ record }">
              <div class="flex items-center gap-3">
                <a-progress :percent="record.progress / 100" size="small" class="flex-1" />
                <span class="text-xs text-[var(--mg-text-3)] w-8">{{ record.progress }}%</span>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime">
            <template #cell="{ record }">
              <span class="text-[var(--mg-text-3)]">{{ record.createTime }}</span>
            </template>
          </a-table-column>
          <a-table-column title="操作" align="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" class="rounded-lg hover:bg-[var(--mg-bg-1)]" @click="router.push(`/task/detail/${record.taskId}`)">详情</a-button>
                <a-button v-if="record.status === 4" type="text" status="success" size="small" class="rounded-lg hover:bg-green-500/10" @click="openShareModal(record)">分享</a-button>
                <a-button v-if="record.status === 5" type="text" status="danger" size="small" class="rounded-lg hover:bg-red-500/10" @click="handleRetry(record.taskId)">重试</a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

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

<style scoped>
.mg-text-gradient {
  background: linear-gradient(135deg, var(--mg-primary) 0%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.glass-card {
  background: var(--mg-bg-card);
  backdrop-filter: blur(20px);
  border: 1px solid var(--mg-border);
}

:deep(.arco-table) {
  background: transparent;
}

:deep(.arco-table-th) {
  background: var(--mg-bg-1) !important;
  color: var(--mg-text-2) !important;
  border-bottom: 1px solid var(--mg-border) !important;
}

:deep(.arco-table-td) {
  background: transparent !important;
  border-bottom: 1px solid var(--mg-border) !important;
}

:deep(.arco-table-tr:hover .arco-table-td) {
  background: var(--mg-bg-1) !important;
}

:deep(.arco-pagination-list-item), :deep(.arco-pagination-item) {
  background: var(--mg-bg-1) !important;
  border: 1px solid var(--mg-border) !important;
  color: var(--mg-text-2) !important;
}

:deep(.arco-pagination-item-active) {
  background: var(--mg-primary) !important;
  border-color: var(--mg-primary) !important;
  color: white !important;
}
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { getTaskList, retryTask } from '@/api/task';
import { shareContent, getCategories } from '@/api/community';

const router = useRouter();
const tasks = ref([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

const getStatusText = (status: number) => {
  const map: any = {
    0: '待处理',
    1: '正在分析',
    2: '正在生成',
    3: '正在验证',
    4: '已完成',
    5: '生成失败',
    6: '超时',
    7: '已取消'
  };
  return map[status] || status;
};

// 分享相关
const shareVisible = ref(false);
const categories = ref<any[]>([]);
const currentTask = ref<any>(null);
const shareForm = ref({
  title: '',
  categoryId: '',
  points: 10,
  tags: [],
  taskId: ''
});

const fetchTasks = async () => {
  loading.value = true;
  try {
    const res: any = await getTaskList({ 
      pageNum: pagination.value.current, 
      pageSize: pagination.value.pageSize 
    });
    tasks.value = res.data.records || res.data.list || [];
    pagination.value.total = res.data.total || 0;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const openShareModal = async (record: any) => {
  currentTask.value = record;
  shareForm.value.title = record.title;
  shareForm.value.taskId = record.taskId;
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
      problemDescription: currentTask.value.problemDescription,
      standardCode: currentTask.value.standardCode
    });
    Message.success('分享成功！已发布到社区广场');
    return true;
  } catch (error) {
    return false;
  }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  fetchTasks();
};

const handleRetry = async (taskId: string) => {
  try {
    const res = await retryTask(taskId);
    Message.success(res.msg || '重试请求已发送');
    fetchTasks();
  } catch (error) {
    console.error(error);
  }
};

const getStatusColor = (status: number) => {
  const map: any = {
    0: 'gray',
    1: 'arcoblue',
    2: 'orange',
    3: 'cyan',
    4: 'green',
    5: 'red',
    6: 'red',
    7: 'gray'
  };
  return map[status] || 'blue';
};

onMounted(fetchTasks);
</script>
