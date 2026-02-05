<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">生成方案管理</h2>
        <p class="text-[var(--mg-text-3)] text-sm">定义AI工作流的执行步骤与提示词模板</p>
      </div>
      <a-button type="primary" size="large" @click="handleAdd" class="rounded-xl shadow-lg shadow-primary/20">
        <template #icon><icon-plus /></template>
        新建方案
      </a-button>
    </div>

    <MgAdminTable 
      ref="tableRef"
      :api="adminWorkflow.list" 
      :search-items="searchItems"
      row-key="id"
    >
      <template #columns>
        <a-table-column title="方案名称" data-index="name" :width="200" />
        <a-table-column title="描述" data-index="description" />
        <a-table-column title="可见性" :width="100">
          <template #cell="{ record }">
            <a-tag :color="record.isVisible ? 'green' : 'gray'">
              {{ record.isVisible ? '公开' : '隐藏' }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="创建时间" data-index="createdAt" :width="180" />
        <a-table-column title="操作" :width="280" align="right">
          <template #cell="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="handleConfigSteps(record)">
                <template #icon><icon-mind-mapping /></template>
                步骤配置
              </a-button>
              <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="text" size="small" @click="handleCopy(record)">复制</a-button>
              <a-popconfirm content="确定删除该方案？" @ok="handleDelete(record)">
                <a-button type="text" status="danger" size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </template>
    </MgAdminTable>

    <!-- 方案基础信息抽屉 -->
    <a-drawer 
      v-model:visible="drawer.visible" 
      :title="drawer.title" 
      width="400px" 
      @ok="handleSubmit"
      unmount-on-close
      class="minimal-drawer"
    >
      <a-form :model="drawer.form" layout="vertical" class="px-2">
        <a-form-item label="方案名称" required field="name">
          <a-input v-model="drawer.form.name" placeholder="例如：Python 算法生成" />
        </a-form-item>
        <a-form-item label="描述" field="description">
          <a-textarea v-model="drawer.form.description" placeholder="方案用途描述..." />
        </a-form-item>
        <a-form-item label="前端可见" field="isVisible">
          <a-switch v-model="drawer.form.isVisible" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space class="w-full justify-end">
          <a-button @click="drawer.visible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <!-- 步骤配置抽屉 -->
    <a-drawer
      v-model:visible="stepsDrawer.visible"
      title="工作流步骤配置"
      width="800px"
      @ok="handleSaveSteps"
      class="minimal-drawer"
    >
      <div class="mb-4 flex justify-between items-center bg-blue-50 p-3 rounded-lg border border-blue-100">
         <span class="text-blue-600 font-bold">当前方案：{{ stepsDrawer.workflowName }}</span>
         <a-button type="outline" size="small" @click="addStep">
           <template #icon><icon-plus /></template> 添加步骤
         </a-button>
      </div>

      <div class="space-y-4">
        <div v-for="(step, index) in stepsDrawer.steps" :key="index" class="p-4 border border-[var(--mg-border)] rounded-xl bg-[var(--mg-bg-1)] relative group hover:border-primary/50 transition-colors">
          <div class="absolute right-2 top-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <a-button type="text" status="danger" size="mini" @click="removeStep(index)">
              <icon-delete />
            </a-button>
          </div>
          
          <div class="flex gap-4 mb-4">
             <div class="w-24">
               <div class="text-xs text-[var(--mg-text-3)] mb-1">步骤顺序</div>
               <a-input-number v-model="step.stepOrder" :min="1" placeholder="顺序" />
             </div>
             <div class="flex-1">
               <div class="text-xs text-[var(--mg-text-3)] mb-1">AI 角色设定 (System Prompt)</div>
               <a-textarea v-model="step.systemPrompt" :auto-size="{minRows: 2, maxRows: 5}" placeholder="你是一个..." />
             </div>
          </div>
          
          <div class="flex gap-4">
             <div class="w-1/3">
               <div class="text-xs text-[var(--mg-text-3)] mb-1">步骤名称/角色名</div>
               <a-input v-model="step.roleName" placeholder="例如：Coder" />
             </div>
             <div class="flex-1">
               <div class="text-xs text-[var(--mg-text-3)] mb-1">用户提示词模板 (User Prompt)</div>
               <a-textarea v-model="step.userPromptTemplate" :auto-size="{minRows: 2, maxRows: 5}" placeholder="请根据以下需求..." />
             </div>
          </div>
        </div>
        
        <div v-if="stepsDrawer.steps.length === 0" class="text-center py-12 text-[var(--mg-text-3)] border border-dashed border-[var(--mg-border)] rounded-xl">
           暂无步骤，请点击右上角添加
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconMindMapping, IconDelete } from '@arco-design/web-vue/es/icon';
import { adminWorkflow } from '@/api/admin';
import MgAdminTable from '@/components/MgAdminTable/index.vue';

const tableRef = ref();
const submitting = ref(false);

const searchItems = [
  { field: 'keyword', label: '搜索', type: 'input', placeholder: '方案名称' }
];

const drawer = ref({ 
  visible: false, 
  title: '新增方案', 
  form: { id: null, name: '', description: '', isVisible: true } 
});

const stepsDrawer = ref({
  visible: false,
  workflowId: null as number | null,
  workflowName: '',
  steps: [] as any[]
});

const handleAdd = () => {
  drawer.value = { 
    visible: true, 
    title: '新增方案', 
    form: { id: null, name: '', description: '', isVisible: true } 
  };
};

const handleEdit = (record: any) => {
  drawer.value = { 
    visible: true, 
    title: '编辑方案', 
    form: { ...record } 
  };
};

const handleSubmit = async () => {
  if (!drawer.value.form.name) return Message.warning('名称不能为空');
  submitting.value = true;
  try {
    if (drawer.value.form.id) {
      await adminWorkflow.update(drawer.value.form);
      Message.success('更新成功');
    } else {
      await adminWorkflow.create(drawer.value.form);
      Message.success('创建成功');
    }
    drawer.value.visible = false;
    tableRef.value?.refresh();
  } catch (error) {} finally {
    submitting.value = false;
  }
};

const handleDelete = async (record: any) => {
  try {
    await adminWorkflow.delete(record.id);
    Message.success('删除成功');
    tableRef.value?.refresh();
  } catch (error) {}
};

const handleCopy = async (record: any) => {
  try {
    await adminWorkflow.copy(record.id);
    Message.success('复制成功');
    tableRef.value?.refresh();
  } catch (error) {}
};

const handleConfigSteps = async (record: any) => {
  stepsDrawer.value.workflowId = record.id;
  stepsDrawer.value.workflowName = record.name;
  stepsDrawer.value.visible = true;
  try {
    const res = await adminWorkflow.getSteps(record.id);
    stepsDrawer.value.steps = res.data || [];
  } catch (error) {
    stepsDrawer.value.steps = [];
  }
};

const addStep = () => {
  stepsDrawer.value.steps.push({
    stepOrder: stepsDrawer.value.steps.length + 1,
    roleName: '',
    systemPrompt: '',
    userPromptTemplate: ''
  });
};

const removeStep = (index: number) => {
  stepsDrawer.value.steps.splice(index, 1);
};

const handleSaveSteps = async () => {
  if (!stepsDrawer.value.workflowId) return;
  try {
    await adminWorkflow.saveSteps(stepsDrawer.value.workflowId, stepsDrawer.value.steps);
    Message.success('步骤配置已保存');
    stepsDrawer.value.visible = false;
  } catch (error) {}
};
</script>

<style scoped>
:deep(.minimal-drawer .arco-drawer-content) {
  background: var(--mg-bg-1);
}
</style>
