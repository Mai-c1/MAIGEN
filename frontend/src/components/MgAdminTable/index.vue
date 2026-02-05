<template>
  <div class="mg-admin-table">
    <!-- 搜索栏 -->
    <a-card v-if="searchItems && searchItems.length > 0" :bordered="false" class="glass-card mb-4">
      <div class="flex flex-wrap gap-4 items-center">
        <template v-for="item in searchItems" :key="item.field">
          <!-- 输入框 -->
          <div v-if="item.type === 'input'" class="flex-1 min-w-[200px] max-w-[300px]">
            <a-input 
              v-model="searchForm[item.field]" 
              :placeholder="item.placeholder || `搜索${item.label}...`" 
              size="large"
              class="rounded-xl"
              allow-clear
              @press-enter="handleSearch"
            />
          </div>
          <!-- 下拉框 -->
          <div v-else-if="item.type === 'select'" class="w-[180px]">
            <a-select 
              v-model="searchForm[item.field]" 
              :placeholder="item.placeholder || item.label" 
              size="large" 
              allow-clear 
              class="rounded-xl"
              @change="handleSearch"
            >
              <a-option v-for="opt in item.options" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </a-option>
            </a-select>
          </div>
        </template>
        
        <a-button type="primary" size="large" class="rounded-xl" @click="handleSearch">
          <template #icon><icon-search /></template>
          查询
        </a-button>
        <a-button size="large" class="rounded-xl" @click="handleReset">
          <template #icon><icon-refresh /></template>
          重置
        </a-button>
        
        <!-- 右侧工具栏插槽 -->
        <div class="ml-auto flex gap-2">
          <slot name="toolbar"></slot>
        </div>
      </div>
    </a-card>

    <!-- 表格区域 -->
    <a-card :bordered="false" class="glass-card">
      <a-table 
        :data="dataList" 
        :loading="loading" 
        :pagination="pagination" 
        @page-change="handlePageChange"
        :bordered="false"
        class="custom-table"
        :row-key="rowKey"
      >
        <template #columns>
          <slot name="columns"></slot>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon';

interface SearchItem {
  field: string;
  label: string;
  type: 'input' | 'select';
  placeholder?: string;
  options?: { label: string; value: any }[];
}

interface Props {
  api: (params: any) => Promise<any>;
  searchItems?: SearchItem[];
  rowKey?: string;
}

const props = withDefaults(defineProps<Props>(), {
  searchItems: () => [],
  rowKey: 'id'
});

const loading = ref(false);
const dataList = ref([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showJumper: true
});

const searchForm = reactive<Record<string, any>>({});

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    };
    const res = await props.api(params);
    const data = res.data;
    if (data) {
      // 兼容 PageResult 和 List 结构
      dataList.value = data.records || data.list || (Array.isArray(data) ? data : []);
      pagination.total = data.total || dataList.value.length;
    }
  } catch (error) {
    console.error('Fetch data failed:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const handleReset = () => {
  Object.keys(searchForm).forEach(key => delete searchForm[key]);
  handleSearch();
};

const handlePageChange = (page: number) => {
  pagination.current = page;
  fetchData();
};

// 暴露给父组件的方法
defineExpose({
  refresh: fetchData,
  handleSearch
});

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.glass-card {
  background: rgba(var(--mg-bg-2), 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid var(--mg-border);
  border-radius: 16px;
}

:deep(.custom-table .arco-table-container) {
  border-radius: 12px;
}
</style>
