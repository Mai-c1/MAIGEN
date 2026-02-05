<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">权限管理</h2>
        <p class="text-[var(--mg-text-3)] text-sm">定义系统功能访问点与API授权策略</p>
      </div>
      <a-button type="primary" size="large" @click="handleAdd" class="rounded-xl shadow-lg shadow-primary/20">
        <template #icon><icon-plus /></template>
        新增权限
      </a-button>
    </div>

    <MgAdminTable 
      ref="tableRef"
      :api="adminPermission.list" 
      :search-items="searchItems"
      row-key="id"
    >
      <template #columns>
        <a-table-column title="ID" data-index="id" :width="80" />
        <a-table-column title="权限名称" data-index="name" :width="200" />
        <a-table-column title="权限标识" data-index="code" :width="200">
          <template #cell="{ record }">
            <a-tag color="arcoblue" class="rounded-md font-mono">{{ record.code }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="描述" data-index="description" />
        <a-table-column title="创建时间" data-index="createdAt" :width="180" />
        <a-table-column title="操作" :width="180" align="right">
          <template #cell="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm content="确定删除该权限？" @ok="handleDelete(record)">
                <a-button type="text" status="danger" size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </template>
    </MgAdminTable>

    <!-- 权限表单抽屉 -->
    <a-drawer 
      v-model:visible="drawer.visible" 
      :title="drawer.title" 
      width="400px" 
      @ok="handleSubmit"
      unmount-on-close
      class="minimal-drawer"
    >
      <a-form :model="drawer.form" layout="vertical" class="px-2">
        <a-form-item label="权限名称" required field="name">
          <a-input v-model="drawer.form.name" placeholder="例如：查看用户" />
        </a-form-item>
        <a-form-item label="权限标识 (Code)" required field="code">
          <a-input v-model="drawer.form.code" placeholder="例如：user:view" />
          <template #help>用于后端 @SaCheckPermission 注解鉴权</template>
        </a-form-item>
        <a-form-item label="描述" field="description">
          <a-textarea v-model="drawer.form.description" placeholder="功能描述..." />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space class="w-full justify-end">
          <a-button @click="drawer.visible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { adminPermission } from '@/api/admin';
import MgAdminTable from '@/components/MgAdminTable/index.vue';

const tableRef = ref();
const submitting = ref(false);

const searchItems = [
  { field: 'keyword', label: '搜索', type: 'input', placeholder: '名称/标识' }
];

const drawer = ref({ 
  visible: false, 
  title: '新增权限', 
  form: { id: null, name: '', code: '', description: '' } 
});

const handleAdd = () => {
  drawer.value = { 
    visible: true, 
    title: '新增权限', 
    form: { id: null, name: '', code: '', description: '' } 
  };
};

const handleEdit = (record: any) => {
  drawer.value = { 
    visible: true, 
    title: '编辑权限', 
    form: { ...record } 
  };
};

const handleSubmit = async () => {
  if (!drawer.value.form.name || !drawer.value.form.code) return Message.warning('请填写必填项');
  submitting.value = true;
  try {
    if (drawer.value.form.id) {
      await adminPermission.update(drawer.value.form);
      Message.success('更新成功');
    } else {
      await adminPermission.create(drawer.value.form);
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
    await adminPermission.delete(record.id);
    Message.success('删除成功');
    tableRef.value?.refresh();
  } catch (error) {}
};
</script>

<style scoped>
:deep(.minimal-drawer .arco-drawer-content) {
  background: var(--mg-bg-1);
}
</style>
