<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">角色管理</h2>
        <p class="text-[var(--mg-text-3)] text-sm">配置系统角色及其对应的功能权限</p>
      </div>
      <a-button type="primary" size="large" @click="handleAdd" class="rounded-xl shadow-lg shadow-primary/20">
        <template #icon><icon-plus /></template>
        新增角色
      </a-button>
    </div>

    <MgAdminTable 
      ref="tableRef"
      :api="adminRole.list" 
      :search-items="searchItems"
      row-key="id"
    >
      <template #columns>
        <a-table-column title="角色名称" data-index="name" :width="200" />
        <a-table-column title="描述" data-index="description" />
        <a-table-column title="创建时间" data-index="createdAt" :width="180" />
        <a-table-column title="操作" :width="200" align="right">
          <template #cell="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm content="确定删除该角色？" @ok="handleDelete(record)">
                <a-button type="text" status="danger" size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </template>
    </MgAdminTable>

    <!-- 角色表单抽屉 -->
    <a-drawer 
      v-model:visible="drawer.visible" 
      :title="drawer.title" 
      width="500px" 
      @ok="handleSubmit"
      unmount-on-close
      class="minimal-drawer"
    >
      <a-form :model="drawer.form" layout="vertical" class="px-2">
        <a-form-item label="角色名称" required field="name">
          <a-input v-model="drawer.form.name" placeholder="例如：运营专员" />
        </a-form-item>
        <a-form-item label="描述" field="description">
          <a-textarea v-model="drawer.form.description" placeholder="角色职责描述..." />
        </a-form-item>
        
        <div class="drawer-section-title mt-6">权限分配</div>
        <a-form-item field="permissionIds">
          <a-transfer 
            :data="allPermissions" 
            :model-value="drawer.form.permissionIds"
            @change="(val) => drawer.form.permissionIds = val"
            :title="['待选权限', '已选权限']"
            show-search
          >
            <template #item="{ label, value }">
               {{ label }}
            </template>
          </a-transfer>
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
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus } from '@arco-design/web-vue/es/icon';
import { adminRole, adminPermission } from '@/api/admin';
import MgAdminTable from '@/components/MgAdminTable/index.vue';

const tableRef = ref();
const submitting = ref(false);
const allPermissions = ref<any[]>([]);

const searchItems = [
  { field: 'keyword', label: '搜索', type: 'input', placeholder: '角色名称' }
];

const drawer = ref({ 
  visible: false, 
  title: '新增角色', 
  form: { id: null, name: '', description: '', permissionIds: [] as string[] } 
});

onMounted(async () => {
  // 加载所有权限供选择
  const res = await adminPermission.listAll();
  if (res.data) {
    allPermissions.value = res.data.map((p: any) => ({
      value: p.id,
      label: `${p.name} (${p.code})`
    }));
  }
});

const handleAdd = () => {
  drawer.value = { 
    visible: true, 
    title: '新增角色', 
    form: { id: null, name: '', description: '', permissionIds: [] } 
  };
};

const handleEdit = async (record: any) => {
  drawer.value = { 
    visible: true, 
    title: '编辑角色', 
    form: { ...record, permissionIds: record.permissionIds || [] } 
  };
};

const handleSubmit = async () => {
  if (!drawer.value.form.name) return Message.warning('角色名称不能为空');
  submitting.value = true;
  try {
    if (drawer.value.form.id) {
      await adminRole.update(drawer.value.form);
      Message.success('更新成功');
    } else {
      await adminRole.create(drawer.value.form);
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
    await adminRole.delete(record.id);
    Message.success('删除成功');
    tableRef.value?.refresh();
  } catch (error) {}
};
</script>

<style scoped>
.drawer-section-title {
  font-size: 12px;
  font-weight: bold;
  color: var(--mg-text-3);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--mg-border);
}

:deep(.minimal-drawer .arco-drawer-content) {
  background: var(--mg-bg-1);
}
</style>
