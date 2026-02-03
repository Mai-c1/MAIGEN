<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">系统管理</h2>
        <p class="text-[var(--mg-text-3)] text-sm">全局参数配置、积分规则及操作审计日志</p>
      </div>
      <a-button v-if="activeTab === 'config'" type="primary" @click="handleAddConfig" class="rounded-xl">
        <template #icon><icon-plus /></template>
        新增配置
      </a-button>
    </div>

    <a-tabs v-model:active-key="activeTab" type="rounded" class="custom-tabs">
      <!-- 系统配置 -->
      <a-tab-pane key="config" title="参数配置">
        <a-card :bordered="false" class="glass-card">
          <a-table :data="configs" :pagination="false" :bordered="false" class="custom-table">
            <template #columns>
              <a-table-column title="配置项名称" data-index="name" :width="200" />
              <a-table-column title="配置编码" data-index="code" :width="200">
                <template #cell="{ record }">
                  <span class="font-mono text-xs text-[var(--mg-text-3)]">{{ record.code }}</span>
                </template>
              </a-table-column>
              <a-table-column title="配置值" :width="300">
                <template #cell="{ record }">
                  <div class="truncate max-w-[280px] text-[var(--mg-text-2)] font-mono text-xs">
                    {{ record.value }}
                  </div>
                </template>
              </a-table-column>
              <a-table-column title="描述" data-index="description" />
              <a-table-column title="操作" align="right" :width="150">
                <template #cell="{ record }">
                  <a-space>
                    <a-button type="text" size="small" class="rounded-lg" @click="handleEditConfig(record)">编辑</a-button>
                    <a-popconfirm content="确定要删除该配置吗？" @ok="deleteConfig(record.id)">
                      <a-button type="text" status="danger" size="small" class="rounded-lg">删除</a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>

      <!-- 积分规则 -->
      <a-tab-pane key="rules" title="积分规则">
        <a-card :bordered="false" class="glass-card">
          <a-table :data="rules" :pagination="false" :bordered="false" class="custom-table">
            <template #columns>
              <a-table-column title="规则名称" data-index="name" :width="200" />
              <a-table-column title="分值 (MAI)">
                <template #cell="{ record }">
                  <a-input-number v-model="record.value" size="small" class="rounded-lg max-w-[120px]" />
                </template>
              </a-table-column>
              <a-table-column title="规则描述" data-index="description" />
              <a-table-column title="操作" align="right" :width="100">
                <template #cell="{ record }">
                  <a-button type="text" size="small" class="rounded-lg" @click="updateRule(record)">保存</a-button>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>

      <!-- 全站积分流水 -->
      <a-tab-pane key="points" title="全站流水">
        <a-card :bordered="false" class="glass-card">
          <a-table :data="pointsRecords" :loading="loading" :pagination="pointsPagination" @page-change="handlePointsPageChange" :bordered="false" class="custom-table">
            <template #columns>
              <a-table-column title="关联用户" data-index="userId">
                <template #cell="{ record }">
                  <a-tag size="small" class="rounded-md">UID: {{ record.userId }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="数额变动">
                <template #cell="{ record }">
                  <span :class="record.amount > 0 ? 'text-green-500' : 'text-red-500'" class="font-bold font-mono">
                    {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
                  </span>
                </template>
              </a-table-column>
              <a-table-column title="业务来源" data-index="source">
                <template #cell="{ record }">
                  <a-tag size="small" color="arcoblue" variant="outline" class="rounded-md">{{ record.source }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="流水描述" data-index="description" />
              <a-table-column title="发生时间" data-index="createdAt">
                <template #cell="{ record }">
                  <span class="text-xs text-[var(--mg-text-3)]">{{ record.createdAt }}</span>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>

      <!-- 操作审计日志 -->
      <a-tab-pane key="logs" title="操作日志">
        <a-card :bordered="false" class="glass-card">
          <a-table :data="logs" :loading="loading" :pagination="logPagination" @page-change="handleLogPageChange" :bordered="false" class="custom-table">
            <template #columns>
              <a-table-column title="操作主体" data-index="userId">
                <template #cell="{ record }">
                  <a-tag size="small" class="rounded-md">UID: {{ record.userId || '系统' }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="所属模块" data-index="module" />
              <a-table-column title="动作说明" data-index="operation" />
              <a-table-column title="执行状态">
                <template #cell="{ record }">
                  <a-badge :status="record.status === 1 ? 'success' : 'danger'" :text="record.status === 1 ? '成功' : '失败'" />
                </template>
              </a-table-column>
              <a-table-column title="响应耗时">
                <template #cell="{ record }">
                  <span class="text-xs font-mono" :class="record.duration > 500 ? 'text-warning' : 'text-[var(--mg-text-3)]'">{{ record.duration }}ms</span>
                </template>
              </a-table-column>
              <a-table-column title="操作时间" data-index="createdAt">
                <template #cell="{ record }">
                  <span class="text-xs text-[var(--mg-text-3)]">{{ record.createdAt }}</span>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <!-- 配置编辑弹窗 -->
    <a-modal 
      v-model:visible="configModalVisible" 
      :title="configForm.id ? '编辑配置' : '新增配置'" 
      @ok="handleConfigSubmit"
      width="80%"
      :ok-loading="submitLoading"
      class="config-modal"
    >
      <a-form :model="configForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="配置项名称" required>
              <a-input v-model="configForm.name" placeholder="请输入配置名称，如：系统标题" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="配置编码" required>
              <a-input v-model="configForm.code" :disabled="!!configForm.id" placeholder="请输入唯一编码，如：SYS_TITLE" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="配置描述">
          <a-textarea v-model="configForm.description" placeholder="请输入该配置项的详细说明" />
        </a-form-item>
        <a-form-item label="配置内容 (支持 Markdown)" required>
          <div class="md-editor-container">
            <MgMdEditor 
              v-model="configForm.value" 
              style="height: 400px"
            />
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { Message } from '@arco-design/web-vue';
import { adminSystem } from '@/api/admin';
import MgMdEditor from '@/components/MgMdEditor.vue';

const loading = ref(false);
const submitLoading = ref(false);
const activeTab = ref('config');
const configs = ref([]);
const rules = ref([]);
const pointsRecords = ref([]);
const logs = ref([]);

const configModalVisible = ref(false);
const configForm = reactive({
  id: null,
  name: '',
  code: '',
  value: '',
  description: ''
});

const pointsPagination = ref({ current: 1, pageSize: 10, total: 0 });
const logPagination = ref({ current: 1, pageSize: 10, total: 0 });

const fetchConfigs = async () => {
  const res: any = await adminSystem.listConfigs();
  configs.value = res.data || [];
};

const fetchRules = async () => {
  const res: any = await adminSystem.listPointsRules();
  rules.value = res.data || [];
};

const fetchPointsRecords = async () => {
  loading.value = true;
  const res: any = await adminSystem.listPointsRecords({ 
    pageNum: pointsPagination.value.current, 
    pageSize: pointsPagination.value.pageSize 
  });
  pointsRecords.value = res.data.records || res.data.list || [];
  pointsPagination.value.total = res.data.total || 0;
  loading.value = false;
};

const fetchLogs = async () => {
  loading.value = true;
  const res: any = await adminSystem.listLogs({ 
    pageNum: logPagination.value.current, 
    pageSize: logPagination.value.pageSize 
  });
  logs.value = res.data.records || res.data.list || [];
  logPagination.value.total = res.data.total || 0;
  loading.value = false;
};

// 配置操作
const handleAddConfig = () => {
  configForm.id = null;
  configForm.name = '';
  configForm.code = '';
  configForm.value = '';
  configForm.description = '';
  configModalVisible.value = true;
};

const handleEditConfig = (record: any) => {
  Object.assign(configForm, record);
  configModalVisible.value = true;
};

const handleConfigSubmit = async () => {
  if (!configForm.name || !configForm.code || !configForm.value) {
    Message.warning('请填写必要信息');
    return false;
  }
  
  submitLoading.value = true;
  try {
    if (configForm.id) {
      await adminSystem.updateConfig(configForm);
      Message.success('配置已更新');
    } else {
      await adminSystem.addConfig(configForm);
      Message.success('配置已新增');
    }
    configModalVisible.value = false;
    fetchConfigs();
  } catch (e) {
    // Error handled by interceptor
  } finally {
    submitLoading.value = false;
  }
};

const deleteConfig = async (id: number) => {
  await adminSystem.deleteConfig(id);
  Message.success('配置已删除');
  fetchConfigs();
};

const updateRule = async (record: any) => {
  await adminSystem.updatePointsRule(record);
  Message.success('积分规则已更新并生效');
};

const handlePointsPageChange = (page: number) => {
  pointsPagination.value.current = page;
  fetchPointsRecords();
};

const handleLogPageChange = (page: number) => {
  logPagination.value.current = page;
  fetchLogs();
};

onMounted(() => {
  fetchConfigs();
  fetchRules();
  fetchPointsRecords();
  fetchLogs();
});
</script>

<style scoped>
:deep(.custom-tabs .arco-tabs-nav-type-rounded .arco-tabs-nav-tab) {
  background: rgba(var(--mg-bg-2-rgb), 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  padding: 4px;
  border-radius: 14px;
}

:deep(.custom-tabs .arco-tabs-tab) {
  border-radius: 10px;
  margin: 0 2px;
}

:deep(.custom-table .arco-table-tr) {
  transition: all 0.2s;
}

:deep(.custom-table .arco-table-tr:hover) {
  background: rgba(var(--mg-primary-rgb), 0.02) !important;
}

.md-editor-container {
  border: 1px solid var(--color-border-2);
  border-radius: 8px;
  overflow: hidden;
}

:deep(.md-editor) {
  --md-bk-color: transparent;
}

:deep(.config-modal .arco-modal) {
  border-radius: 16px;
  overflow: hidden;
  background: var(--mg-bg-1);
}
</style>
