<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">任务监控</h2>
        <p class="text-[var(--mg-text-3)] text-sm">全站 AI 生成任务运行状态及资源消耗审计</p>
      </div>
      <a-button type="outline" size="large" class="rounded-xl" disabled>
        <template #icon><icon-command /></template>
        方案管理
      </a-button>
    </div>

    <a-card :bordered="false" class="glass-card mb-6">
      <div class="flex flex-wrap gap-4 items-center">
        <div class="flex-1 min-w-[280px]">
          <a-input-search 
            v-model="searchKeyword" 
            placeholder="搜索任务标题、ID..." 
            size="large"
            class="search-input"
            @search="fetchData"
            allow-clear
          />
        </div>
        <a-select v-model="filterStatus" placeholder="任务状态过滤" size="large" style="width: 180px" @change="fetchData" allow-clear class="rounded-xl">
          <a-option :value="0">排队等待</a-option>
          <a-option :value="1">正在执行</a-option>
          <a-option :value="2">已成功完成</a-option>
          <a-option :value="3">执行失败</a-option>
        </a-select>
        <a-button type="text" size="large" @click="fetchData">
          <template #icon><icon-refresh /></template>
        </a-button>
      </div>
    </a-card>
      
    <a-card :bordered="false" class="glass-card">
      <a-table 
        :data="list" 
        :loading="loading" 
        :pagination="pagination" 
        @page-change="handlePageChange"
        :bordered="false"
        class="custom-table"
      >
        <template #columns>
          <a-table-column title="任务标题" :width="280">
            <template #cell="{ record }">
              <div class="font-bold text-[var(--mg-text-1)] truncate">{{ record.title }}</div>
              <div class="text-[10px] text-[var(--mg-text-3)] mt-0.5 font-mono">{{ record.id }}</div>
            </template>
          </a-table-column>
          <a-table-column title="所属用户" data-index="userId" :width="100">
            <template #cell="{ record }">
              <a-tag size="small" class="rounded-md">UID: {{ record.userId }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="当前状态" :width="140">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.status)" class="rounded-lg border-none">
                <template #icon>
                  <icon-sync v-if="record.status === 1" spin />
                  <icon-check-circle-fill v-else-if="record.status === 2" />
                  <icon-exclamation-circle-fill v-else-if="record.status === 3" />
                  <icon-clock-circle v-else />
                </template>
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="执行进度" :width="200">
            <template #cell="{ record }">
              <div class="flex items-center gap-3">
                <a-progress 
                  :percent="record.progress / 100" 
                  size="small" 
                  :status="record.status === 3 ? 'danger' : 'success'"
                  :animation="record.status === 1"
                  class="flex-1"
                />
                <span class="text-xs font-mono text-[var(--mg-text-3)]">{{ record.progress }}%</span>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createdAt" :width="180">
            <template #cell="{ record }">
              <span class="text-xs text-[var(--mg-text-3)]">{{ record.createdAt }}</span>
            </template>
          </a-table-column>
          <a-table-column title="管理操作" align="right" :width="150">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" class="rounded-lg" @click="showDetail(record)">详情</a-button>
                <a-popconfirm content="强制从系统中移除此任务记录？" @ok="handleDelete(record)">
                  <a-button type="text" status="danger" size="small" class="rounded-lg">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 任务详情抽屉 -->
    <a-drawer 
      v-model:visible="detailDrawer.visible" 
      title="生成任务审计" 
      width="480px" 
      :footer="false"
      class="minimal-drawer"
    >
      <div v-if="detailDrawer.record" class="space-y-6">
        <div class="p-4 bg-primary/5 rounded-2xl border border-primary/10">
          <div class="text-xs text-primary mb-1">任务全称：</div>
          <div class="font-bold text-lg text-[var(--mg-text-1)]">{{ detailDrawer.record.title }}</div>
        </div>
        
        <div class="grid grid-cols-2 gap-4">
          <div class="p-3 bg-[var(--mg-bg-1)] rounded-xl border border-[var(--mg-border)]">
            <div class="text-[10px] text-[var(--mg-text-3)] mb-1 uppercase">任务 ID</div>
            <div class="font-mono text-sm text-[var(--mg-text-2)]">{{ detailDrawer.record.id }}</div>
          </div>
          <div class="p-3 bg-[var(--mg-bg-1)] rounded-xl border border-[var(--mg-border)]">
            <div class="text-[10px] text-[var(--mg-text-3)] mb-1 uppercase">所属用户 ID</div>
            <div class="font-mono text-sm text-[var(--mg-text-2)]">{{ detailDrawer.record.userId }}</div>
          </div>
        </div>

        <div>
          <div class="drawer-section-title">任务需求描述</div>
          <div class="text-sm text-[var(--mg-text-2)] bg-[var(--mg-bg-1)] p-4 rounded-xl border border-[var(--mg-border)] whitespace-pre-wrap leading-relaxed">
            {{ detailDrawer.record.problemDescription }}
          </div>
        </div>

        <div v-if="detailDrawer.record.errorMessage">
          <div class="drawer-section-title text-red-500">执行异常信息</div>
          <div class="text-xs text-red-400 font-mono bg-red-500/5 p-4 rounded-xl border border-red-500/10 whitespace-pre-wrap">
            {{ detailDrawer.record.errorMessage }}
          </div>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { Message } from '@arco-design/web-vue';
import { 
  IconRefresh, 
  IconCommand, 
  IconSync, 
  IconCheckCircleFill, 
  IconExclamationCircleFill, 
  IconClockCircle,
  IconDelete
} from '@arco-design/web-vue/es/icon';
import { adminTask } from '@/api/admin';

const loading = ref(false);
const list = ref([]);
const searchKeyword = ref('');
const filterStatus = ref(null);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const detailDrawer = ref({ visible: false, record: null });

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await adminTask.list({ 
      pageNum: pagination.value.current, 
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value,
      status: filterStatus.value
    });
    list.value = res.data.records || res.data.list || [];
    pagination.value.total = res.data.total || 0;
  } catch (error) {} finally { loading.value = false; }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  fetchData();
};

const getStatusColor = (status: number) => {
  const map: any = { 0: 'gray', 1: 'arcoblue', 2: 'green', 3: 'red' };
  return map[status] || 'blue';
};

const getStatusText = (status: number) => {
  const map: any = { 0: '队列中', 1: '执行中', 2: '已完成', 3: '异常' };
  return map[status] || '未知';
};

const showDetail = (record: any) => {
  detailDrawer.value = { visible: true, record };
};

const handleDelete = async (record: any) => {
  try {
    await adminTask.delete(record.id);
    Message.success('任务记录已从系统中清除');
    fetchData();
  } catch (error) {}
};

onMounted(fetchData);
</script>

<style scoped>
.search-input :deep(.arco-input-wrapper) {
  border-radius: 16px;
  background: var(--mg-bg-1);
  border: 1px solid var(--mg-border);
}

.drawer-section-title {
  font-size: 11px;
  font-weight: bold;
  color: var(--mg-text-3);
  text-transform: uppercase;
  letter-spacing: 1.5px;
  margin: 24px 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-section-title::after {
  content: "";
  flex: 1;
  height: 1px;
  background: var(--mg-border);
}

:deep(.minimal-drawer .arco-drawer-content) {
  background: var(--mg-bg-1);
}
</style>
