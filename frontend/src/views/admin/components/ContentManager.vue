<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">内容管理</h2>
        <p class="text-[var(--mg-text-3)] text-sm">社区资源审核、分类目录及全局标签维护</p>
      </div>
      <a-space size="medium">
        <a-button type="outline" size="large" @click="showCategoryDrawer" class="rounded-xl">
          <template #icon><icon-apps /></template>
          分类管理
        </a-button>
        <a-button type="outline" size="large" @click="showTagDrawer" class="rounded-xl">
          <template #icon><icon-tags /></template>
          标签管理
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="glass-card mb-6">
      <div class="flex items-center gap-4">
        <a-radio-group v-model="filterStatus" type="button" size="large" @change="fetchData" class="custom-radio-group">
          <a-radio :value="0">待审核</a-radio>
          <a-radio :value="1">已通过</a-radio>
          <a-radio :value="2">已驳回</a-radio>
        </a-radio-group>
        <div class="flex-1"></div>
        <a-button type="text" @click="fetchData">
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
          <a-table-column title="资源标题" :width="300">
            <template #cell="{ record }">
              <div class="font-bold text-[var(--mg-text-1)]">{{ record.title }}</div>
              <div class="text-xs text-[var(--mg-text-3)] mt-1 truncate max-w-[280px]">{{ record.description }}</div>
            </template>
          </a-table-column>
          <a-table-column title="发布者 ID" data-index="userId" />
          <a-table-column title="当前状态">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.status)" class="rounded-lg">
                <template #icon>
                  <icon-check-circle v-if="record.status === 1" />
                  <icon-close-circle v-else-if="record.status === 2" />
                  <icon-sync v-else spin />
                </template>
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="发布时间" data-index="createdAt" :width="180">
            <template #cell="{ record }">
              <span class="text-xs text-[var(--mg-text-3)]">{{ record.createdAt }}</span>
            </template>
          </a-table-column>
          <a-table-column title="管理操作" align="right" :width="200">
            <template #cell="{ record }">
              <a-space>
                <a-button v-if="record.status === 0" type="text" status="success" size="small" class="rounded-lg" @click="handleAudit(record, 'PASS')">通过</a-button>
                <a-button v-if="record.status === 0" type="text" status="danger" size="small" class="rounded-lg" @click="handleAudit(record, 'REJECT')">驳回</a-button>
                <a-popconfirm content="确定从全站移除此内容？" @ok="handleDelete(record)">
                  <a-button type="text" status="danger" size="small" class="rounded-lg">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 内容审核抽屉 -->
    <a-drawer 
      v-model:visible="auditDrawer.visible" 
      title="内容合规性审计" 
      width="400px" 
      @ok="submitAudit"
      class="minimal-drawer"
    >
      <div v-if="auditDrawer.record" class="mb-6 p-4 bg-primary/5 rounded-2xl border border-primary/10">
        <div class="text-xs text-primary mb-1">正在审核资源：</div>
        <div class="font-bold text-[var(--mg-text-1)]">{{ auditDrawer.record.title }}</div>
      </div>
      <a-form :model="auditDrawer.form" layout="vertical">
        <a-form-item label="审核操作">
          <a-radio-group v-model="auditDrawer.status" type="button" class="w-full">
            <a-radio value="PASS" class="flex-1 text-center">准予发布</a-radio>
            <a-radio value="REJECT" class="flex-1 text-center">驳回申请</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="审核备注/反馈建议">
          <a-textarea 
            v-model="auditDrawer.form.reason" 
            placeholder="若驳回，请告知作者具体修改建议..." 
            :auto-size="{ minRows: 4 }"
            class="rounded-xl"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space class="w-full justify-end">
          <a-button @click="auditDrawer.visible = false">取消</a-button>
          <a-button type="primary" @click="submitAudit">提交审核结果</a-button>
        </a-space>
      </template>
    </a-drawer>

    <!-- 分类管理抽屉 -->
    <a-drawer v-model:visible="categoryDrawer.visible" title="资源分类管理" width="400px" class="minimal-drawer">
      <div class="mb-6 flex gap-2">
        <a-input v-model="newCategoryName" placeholder="输入新分类名称..." class="rounded-xl" />
        <a-button type="primary" @click="addCategory" class="rounded-xl">
          <template #icon><icon-plus /></template>
        </a-button>
      </div>
      <div class="category-list">
        <div v-for="item in categories" :key="item.id" class="category-item group">
          <span class="text-[var(--mg-text-1)]">{{ item.name }}</span>
          <a-button type="text" status="danger" size="small" class="opacity-0 group-hover:opacity-100 transition-opacity" @click="deleteCategory(item.id)">
            <template #icon><icon-delete /></template>
          </a-button>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { 
  IconRefresh, 
  IconCheckCircle, 
  IconCloseCircle, 
  IconSync, 
  IconApps, 
  IconTags,
  IconPlus,
  IconDelete
} from '@arco-design/web-vue/es/icon';
import { adminContent } from '@/api/admin';

const loading = ref(false);
const list = ref([]);
const filterStatus = ref(0);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const auditDrawer = ref({ visible: false, record: null, status: 'PASS', form: { reason: '' } });
const categoryDrawer = ref({ visible: false });
const categories = ref([]);
const newCategoryName = ref('');

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await adminContent.list({ 
      pageNum: pagination.value.current, 
      pageSize: pagination.value.pageSize,
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
  if (status === 1) return 'green';
  if (status === 2) return 'red';
  return 'orange';
};

const getStatusText = (status: number) => {
  if (status === 1) return '已通过';
  if (status === 2) return '已驳回';
  return '待审核';
};

const handleAudit = (record: any, status: string) => {
  auditDrawer.value = { visible: true, record, status, form: { reason: '' } };
};

const submitAudit = async () => {
  try {
    await adminContent.audit(auditDrawer.value.record.id, { 
      status: auditDrawer.value.status, 
      reason: auditDrawer.value.form.reason 
    });
    Message.success('审核处理完成');
    auditDrawer.value.visible = false;
    fetchData();
  } catch (error) {}
};

const handleDelete = async (record: any) => {
  try {
    await adminContent.delete(record.id);
    Message.success('资源已成功移除');
    fetchData();
  } catch (error) {}
};

const showCategoryDrawer = async () => {
  categoryDrawer.value.visible = true;
  const res: any = await adminContent.listCategories();
  categories.value = res.data || [];
};

const addCategory = async () => {
  if (!newCategoryName.value) return;
  await adminContent.createCategory({ name: newCategoryName.value });
  newCategoryName.value = '';
  showCategoryDrawer();
};

const deleteCategory = async (id: number) => {
  await adminContent.deleteCategory(id);
  showCategoryDrawer();
};

const showTagDrawer = () => {
  Message.info('标签管理功能即将上线');
};

onMounted(fetchData);
</script>

<style scoped>
.custom-radio-group :deep(.arco-radio-button) {
  background: var(--mg-bg-1);
  border: 1px solid var(--mg-border);
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 8px;
  background: var(--mg-bg-card);
  border: 1px solid var(--mg-border);
  transition: all 0.2s;
}

.category-item:hover {
  background: var(--mg-bg-1);
  border-color: var(--mg-primary);
}

:deep(.minimal-drawer .arco-drawer-content) {
  background: var(--mg-bg-1);
}
</style>
