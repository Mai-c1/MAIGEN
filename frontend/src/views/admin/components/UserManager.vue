<template>
  <div class="module-container">
    <div class="mb-6 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">用户管理</h2>
        <p class="text-[var(--mg-text-3)] text-sm">全站用户信息、权限及积分流水管控</p>
      </div>
      <a-button type="primary" size="large" @click="handleAdd" class="rounded-xl shadow-lg shadow-primary/20">
        <template #icon><icon-plus /></template>
        新增用户
      </a-button>
    </div>

    <a-card :bordered="false" class="glass-card mb-6">
      <div class="flex flex-wrap gap-4 items-center">
        <div class="flex-1 min-w-[300px]">
          <a-input-search 
            v-model="searchKeyword" 
            placeholder="通过用户名、邮箱进行全局搜索..." 
            size="large"
            class="search-input"
            @search="fetchData"
            allow-clear
          />
        </div>
        <a-space>
          <a-button type="outline" size="large" class="rounded-xl" @click="fetchData">
            <template #icon><icon-refresh /></template>
            刷新
          </a-button>
        </a-space>
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
          <a-table-column title="用户信息" :width="280">
            <template #cell="{ record }">
              <div class="flex items-center gap-3">
                <a-avatar :size="40" class="bg-primary/10 text-primary border border-primary/20">
                  {{ record.username[0].toUpperCase() }}
                </a-avatar>
                <div class="overflow-hidden">
                  <div class="font-bold text-[var(--mg-text-1)] truncate">{{ record.username }}</div>
                  <div class="text-xs text-[var(--mg-text-3)] truncate">{{ record.email }}</div>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="系统角色">
            <template #cell="{ record }">
              <a-tag :color="record.role === 'admin' ? 'purple' : 'arcoblue'" class="rounded-lg px-3">
                <template #icon><icon-user v-if="record.role !== 'admin'" /><icon-settings v-else /></template>
                {{ record.role === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="资产余额">
            <template #cell="{ record }">
              <div class="flex flex-col">
                <span class="font-bold text-yellow-500">{{ (record.points || 0).toLocaleString() }} MAI</span>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="账户状态">
            <template #cell="{ record }">
              <a-badge :status="record.status === 1 ? 'success' : 'danger'" :text="record.status === 1 ? '正常' : '已锁定'" />
            </template>
          </a-table-column>
          <a-table-column title="注册时间" data-index="createdAt" :width="180">
            <template #cell="{ record }">
              <span class="text-xs text-[var(--mg-text-3)]">{{ record.createdAt }}</span>
            </template>
          </a-table-column>
          <a-table-column title="管理操作" :width="200" align="right">
            <template #cell="{ record }">
              <a-space>
                <a-button type="text" size="small" class="rounded-lg hover:bg-primary/10" @click="handleEdit(record)">编辑</a-button>
                <a-button type="text" size="small" status="warning" class="rounded-lg hover:bg-warning/10" @click="handleAdjustPoints(record)">调账</a-button>
                <a-popconfirm :content="record.status === 1 ? '确定锁定该用户？' : '确定解锁该用户？'" @ok="toggleStatus(record)">
                  <a-button type="text" :status="record.status === 1 ? 'danger' : 'success'" size="small" class="rounded-lg">
                    {{ record.status === 1 ? '锁定' : '解锁' }}
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 用户管理抽屉 (Minimalist Drawer) -->
    <a-drawer 
      v-model:visible="drawer.visible" 
      :title="drawer.title" 
      width="400px" 
      @ok="handleSubmit"
      unmount-on-close
      :footer="true"
      class="minimal-drawer"
    >
      <a-form :model="drawer.form" layout="vertical" class="px-2">
        <div class="drawer-section-title">基础身份信息</div>
        <a-form-item label="用户名" required field="username">
          <a-input v-model="drawer.form.username" :disabled="!!drawer.form.id" placeholder="唯一识别用户名" />
        </a-form-item>
        <a-form-item label="电子邮箱" field="email">
          <a-input v-model="drawer.form.email" placeholder="user@example.com" />
        </a-form-item>
        
        <div class="drawer-section-title mt-6">安全与权限</div>
        <a-form-item label="账户密码" :required="!drawer.form.id" field="password">
          <a-input-password v-model="drawer.form.password" :placeholder="drawer.form.id ? '留空表示不修改密码' : '设置初始登录密码'" />
        </a-form-item>
        <a-form-item label="系统角色分派" field="role">
          <a-radio-group v-model="drawer.form.role" type="button" class="w-full">
            <a-radio value="user" class="flex-1 text-center">普通用户</a-radio>
            <a-radio value="admin" class="flex-1 text-center">管理员</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space class="w-full justify-end">
          <a-button @click="drawer.visible = false">取消</a-button>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">确认保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <!-- 积分调账抽屉 -->
    <a-drawer 
      v-model:visible="pointsDrawer.visible" 
      title="资产调账流水" 
      width="380px" 
      @ok="handlePointsSubmit"
      class="minimal-drawer"
    >
      <div class="mb-6 p-4 bg-yellow-500/5 rounded-2xl border border-yellow-500/10">
        <div class="text-xs text-yellow-600 mb-1">正在为以下用户调整资产：</div>
        <div class="font-bold text-lg text-[var(--mg-text-1)]">{{ pointsDrawer.username }}</div>
      </div>
      <a-form :model="pointsDrawer.form" layout="vertical">
        <a-form-item label="调整数额" required>
          <a-input-number 
            v-model="pointsDrawer.form.amount" 
            placeholder="正数增加，负数减少" 
            :precision="0"
            size="large"
            class="rounded-xl"
          >
            <template #append>MAI</template>
          </a-input-number>
        </a-form-item>
        <a-form-item label="调账原因/备注" required>
          <a-textarea 
            v-model="pointsDrawer.form.reason" 
            placeholder="请输入调账原因，该信息将展示在用户的流水记录中..." 
            :auto-size="{ minRows: 4 }"
            class="rounded-xl"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space class="w-full justify-end">
          <a-button @click="pointsDrawer.visible = false">放弃</a-button>
          <a-button type="primary" status="warning" @click="handlePointsSubmit">确认调账</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Message } from '@arco-design/web-vue';
import { IconPlus, IconRefresh, IconUser, IconSettings } from '@arco-design/web-vue/es/icon';
import { adminUser } from '@/api/admin';

const loading = ref(false);
const submitting = ref(false);
const list = ref([]);
const searchKeyword = ref('');
const pagination = ref({ current: 1, pageSize: 10, total: 0 });

const drawer = ref({ visible: false, title: '新增用户', form: { id: null, username: '', email: '', password: '', role: 'user' } });
const pointsDrawer = ref({ visible: false, userId: null, username: '', form: { amount: 0, reason: '' } });

const fetchData = async () => {
  loading.value = true;
  try {
    const res: any = await adminUser.list({ 
      pageNum: pagination.value.current, 
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value 
    });
    list.value = res.data.records || res.data.list || [];
    pagination.value.total = res.data.total || 0;
  } catch (error) {
    Message.error('数据加载失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  fetchData();
};

const handleAdd = () => {
  drawer.value = { visible: true, title: '创建新系统用户', form: { id: null, username: '', email: '', password: '', role: 'user' } };
};

const handleEdit = (record: any) => {
  drawer.value = { 
    visible: true, 
    title: '修改用户信息', 
    form: { ...record, password: '' } 
  };
};

const handleSubmit = async () => {
  if (!drawer.value.form.username) return Message.warning('用户名不能为空');
  submitting.value = true;
  try {
    if (drawer.value.form.id) {
      await adminUser.update(drawer.value.form.id, drawer.value.form);
      Message.success('用户信息更新成功');
    } else {
      await adminUser.create(drawer.value.form);
      Message.success('用户创建成功');
    }
    drawer.value.visible = false;
    fetchData();
  } catch (error) {} finally {
    submitting.value = false;
  }
};

const toggleStatus = async (record: any) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1;
    await adminUser.updateStatus(record.id, newStatus);
    Message.success(newStatus === 1 ? '用户已成功解锁' : '用户已被系统锁定');
    fetchData();
  } catch (error) {}
};

const handleAdjustPoints = (record: any) => {
  pointsDrawer.value = { visible: true, userId: record.id, username: record.username, form: { amount: 0, reason: '' } };
};

const handlePointsSubmit = async () => {
  if (!pointsDrawer.value.form.reason) return Message.warning('请务必填写调账原因');
  try {
    await adminUser.adjustPoints(pointsDrawer.value.userId!, pointsDrawer.value.form);
    Message.success('资产调整指令已生效');
    pointsDrawer.value.visible = false;
    fetchData();
  } catch (error) {}
};

onMounted(fetchData);
</script>

<style scoped>
.search-input :deep(.arco-input-wrapper) {
  border-radius: 16px;
  background: rgba(var(--mg-bg-2-rgb), 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.drawer-section-title {
  font-size: 12px;
  font-weight: bold;
  color: var(--mg-text-3);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

:deep(.minimal-drawer .arco-drawer-content) {
  background: var(--mg-bg-1);
}

:deep(.custom-table .arco-table-tr) {
  transition: all 0.2s;
}

:deep(.custom-table .arco-table-tr:hover) {
  background: rgba(var(--mg-primary-rgb), 0.02) !important;
}
</style>
