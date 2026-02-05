<template>
  <div class="module-container">
    <div class="mb-6">
      <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">系统操作日志</h2>
      <p class="text-[var(--mg-text-3)] text-sm">审计系统关键操作与异常记录</p>
    </div>

    <MgAdminTable 
      ref="tableRef"
      :api="adminSystem.listLogs" 
      :search-items="searchItems"
      row-key="id"
    >
      <template #columns>
        <a-table-column title="ID" data-index="id" :width="80" />
        <a-table-column title="操作人ID" data-index="userId" :width="100" />
        <a-table-column title="模块" data-index="module" :width="120">
          <template #cell="{ record }">
            <a-tag>{{ record.module }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="操作类型" data-index="operation" :width="150" />
        <a-table-column title="请求方法" data-index="method" :width="100" />
        <a-table-column title="请求参数" :width="200">
           <template #cell="{ record }">
             <a-popover title="完整参数" trigger="click">
               <div class="truncate cursor-pointer text-[var(--mg-text-3)] max-w-[180px]">{{ record.params }}</div>
               <template #content>
                 <div class="max-w-[400px] max-h-[300px] overflow-auto break-all font-mono text-xs">
                   {{ record.params }}
                 </div>
               </template>
             </a-popover>
           </template>
        </a-table-column>
        <a-table-column title="IP地址" data-index="ip" :width="140" />
        <a-table-column title="耗时" data-index="duration" :width="100">
          <template #cell="{ record }">
            <span :class="record.duration > 1000 ? 'text-red-500' : 'text-green-600'">
              {{ record.duration }}ms
            </span>
          </template>
        </a-table-column>
        <a-table-column title="状态" :width="100">
          <template #cell="{ record }">
            <a-badge :status="record.status === 1 ? 'success' : 'danger'" :text="record.status === 1 ? '成功' : '失败'" />
          </template>
        </a-table-column>
        <a-table-column title="操作时间" data-index="createdAt" :width="180" />
      </template>
    </MgAdminTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { adminSystem } from '@/api/admin';
import MgAdminTable from '@/components/MgAdminTable/index.vue';

const tableRef = ref();

const searchItems = [
  { field: 'module', label: '模块', type: 'input', placeholder: '例如：用户管理' },
  { field: 'userId', label: '用户ID', type: 'input', placeholder: '用户ID' },
  { 
    field: 'status', 
    label: '状态', 
    type: 'select', 
    options: [
      { label: '成功', value: 1 },
      { label: '失败', value: 0 }
    ] 
  }
];
</script>
