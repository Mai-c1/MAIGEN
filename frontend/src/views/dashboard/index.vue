<template>
  <div class="dashboard-container">
    <!-- 欢迎头部 -->
    <div class="welcome-header mb-8 flex justify-between items-center glass-card p-8 rounded-[32px] relative overflow-hidden border-none">
      <!-- 背景装饰 -->
      <div class="absolute -right-20 -top-20 w-64 h-64 bg-primary/15 rounded-full blur-3xl animate-pulse"></div>
      <div class="absolute -left-10 -bottom-10 w-40 h-40 bg-purple-500/10 rounded-full blur-2xl"></div>
      
      <div class="flex items-center gap-6 relative z-10">
        <a-avatar :size="80" class="shadow-2xl border-4 border-primary/20 ring-4 border-[var(--mg-border)] transition-transform hover:scale-105 duration-500">
          <img 
            :src="userStore.userInfo?.avatar || 'https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f1341c7918341.png~tplv-uwbnlip3yd-webp.webp'" 
            style="object-fit: cover; width: 100%; height: 100%;"
          />
        </a-avatar>
        <div>
          <h1 class="text-3xl font-black text-[var(--mg-text-1)] mb-2 tracking-tight">
            {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '开发者' }}
          </h1>
          <p class="text-[var(--mg-text-2)] text-lg m-0 opacity-80 font-medium">欢迎回来！</p>
        </div>
      </div>
      
      <div class="relative z-10">
        <a-button type="primary" size="large" @click="router.push('/task/create')" class="rounded-2xl h-14 px-10">
          <template #icon><icon-plus /></template>
          新建生成任务
        </a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <a-grid :cols="{ xs: 1, sm: 2, md: 4 }" :col-gap="20" :row-gap="20" class="mb-8">
      <a-grid-item v-for="item in stats" :key="item.title">
        <a-card class="glass-card stats-card h-full" :bordered="false">
          <div class="flex items-center gap-4">
            <div :class="['icon-box', item.color]">
              <component :is="iconMap[item.icon]" :style="{ fontSize: '24px' }" />
            </div>
            <div>
              <div class="text-[var(--mg-text-2)] text-sm font-medium mb-1">{{ item.title }}</div>
              <div class="flex items-baseline gap-1">
                <span class="text-2xl font-bold text-[var(--mg-text-1)]">{{ item.value }}</span>
                <span class="text-xs text-[var(--mg-text-3)] font-normal">{{ item.unit }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </a-grid-item>
    </a-grid>

    <a-row :gutter="20" class="mb-8">
      <!-- 左侧：最近活动 -->
      <a-col :span="14">
        <a-card class="glass-card h-full" :bordered="false">
          <template #title>
            <div class="flex items-center gap-2">
              <icon-thunderbolt class="text-primary" />
              <span>最近活动</span>
            </div>
          </template>
          <template #extra>
            <a-link @click="router.push('/task/list')">查看全部任务 <icon-right /></a-link>
          </template>
          
          <a-list :bordered="false" :loading="loading">
            <a-list-item 
              v-for="item in latestTasks" 
              :key="item.taskId" 
              class="activity-item hover:bg-[var(--mg-bg-1)] transition-colors rounded-xl px-4 cursor-pointer"
              @click="router.push(`/task/detail/${item.taskId}`)"
            >
              <a-list-item-meta
                :title="item.title"
                :description="`任务 ID: ${item.taskId} · 创建于 ${item.createTime}`"
              >
                <template #title>
                  <span class="text-[var(--mg-text-1)] font-bold">{{ item.title }}</span>
                </template>
                <template #avatar>
                  <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                    <icon-code v-if="item.status === 4" />
                    <icon-loading v-else-if="item.status >= 1 && item.status <= 3" spin />
                    <icon-exclamation-circle v-else />
                  </div>
                </template>
              </a-list-item-meta>
              <template #actions>
                <div class="flex items-center gap-4">
                  <a-progress 
                    v-if="item.status >= 1 && item.status <= 3"
                    type="circle" 
                    :percent="item.progress / 100" 
                    size="mini" 
                    :stroke-width="4"
                  />
                  <a-tag :color="getStatusColor(item.status)" class="rounded-lg px-3">
                    {{ getStatusText(item.status) }}
                  </a-tag>
                  <a-button type="text" @click="router.push(`/task/detail/${item.taskId}`)">
                    <template #icon><icon-right /></template>
                  </a-button>
                </div>
              </template>
            </a-list-item>
            <template #empty>
              <div class="py-8 text-center">
                <a-empty description="暂无活动数据" />
              </div>
            </template>
          </a-list>
        </a-card>
      </a-col>
      <!-- 右侧：每日签到 -->
      <a-col :span="10">
        <a-card title="每日签到" :bordered="false" class="glass-card h-full">
          <div class="flex flex-col h-full">
            <div class="signin-progress-wrap">
              <!-- 累计签到展示 -->
              <div class="text-center mb-6">
                <div class="text-[var(--mg-text-3)] text-sm mb-1">本月累计签到</div>
                <div class="flex items-baseline justify-center gap-2">
                  <span class="text-5xl font-black mg-text-gradient">{{ totalSignedDays }}</span>
                  <span class="text-xl font-bold text-[var(--mg-text-1)]">天</span>
                </div>
              </div>

              <!-- 7天周期进度条 -->
              <div class="flex justify-between items-center mb-8 px-2">
                <div v-for="(day, index) in weeklyDays" :key="index" class="flex flex-col items-center gap-2">
                  <div 
                    class="w-10 h-10 rounded-full flex items-center justify-center transition-all duration-300"
                    :class="[
                      day.signed ? 'bg-primary text-white shadow-lg shadow-primary/30' : 'bg-[var(--mg-bg-1)] text-[var(--mg-text-3)] border border-[var(--mg-border)]',
                      day.isToday && !day.signed ? 'ring-2 ring-primary ring-offset-2 ring-offset-transparent animate-pulse' : ''
                    ]"
                  >
                    <icon-check v-if="day.signed" :style="{ fontSize: '20px' }" />
                    <span v-else class="text-xs font-bold">{{ day.points }}</span>
                  </div>
                  <span class="text-[var(--mg-text-3)] text-[10px]">{{ day.label }}</span>
                </div>
              </div>

              <!-- 签到按钮 -->
              <a-button 
                type="primary" 
                size="large" 
                long 
                class="rounded-2xl h-14 shadow-lg transition-all duration-200"
                :class="hasSignedIn ? 'opacity-60 grayscale' : 'shadow-primary/20 hover:scale-[1.02] active:scale-[0.98]'"
                :disabled="hasSignedIn"
                @click="handleSignIn"
              >
                <template #icon>
                  <icon-check-circle-fill v-if="hasSignedIn" />
                  <icon-thunderbolt v-else />
                </template>
                {{ hasSignedIn ? '今日已打卡，明天再来哦' : '立即签到领取 5 积分' }}
              </a-button>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 积分记录抽屉 -->
    <a-drawer :visible="showRecords" @cancel="showRecords = false" title="积分流水记录" :width="420" :footer="false" class="glass-card">
      <a-list :bordered="false" :loading="recordsLoading">
        <a-list-item v-for="record in records" :key="record.id" class="border-b border-[var(--mg-border)] last:border-0">
          <a-list-item-meta
            :title="record.sourceName"
            :description="record.createdAt"
          >
            <template #avatar>
              <div :class="record.amount > 0 ? 'bg-green-500/10 text-green-500' : 'bg-red-500/10 text-red-500'" class="w-12 h-12 rounded-xl flex items-center justify-center font-bold">
                {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
              </div>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </a-list>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { useUserStore } from '@/store/user';
import * as echarts from 'echarts';
import { 
  IconSafe, 
  IconCode,
  IconCheckCircle,
  IconRight,
  IconPlus,
  IconThunderbolt,
  IconLoading,
  IconExclamationCircle,
  IconCheck
} from '@arco-design/web-vue/es/icon';
import { getPointsBalance, signIn, getPointsRecords, getMonthSignInDays } from '@/api/points';
import { getTaskList, getTaskStatistics } from '@/api/task';

const router = useRouter();
const userStore = useUserStore();

const pointsBalance = ref(0);
const latestTasks = ref<any[]>([]);
const loading = ref(false);
const showRecords = ref(false);
const records = ref<any[]>([]);
const recordsLoading = ref(false);

const iconMap: Record<string, any> = {
  IconSafe,
  IconCode,
  IconCheckCircle,
  IconLoading,
  IconExclamationCircle,
  IconThunderbolt
};

const stats = ref([
  { title: '积分余额', value: '0', unit: 'MAI', icon: 'IconSafe', color: 'text-yellow-500 bg-yellow-500/10' },
  { title: '进行中任务', value: '0', unit: '个', icon: 'IconLoading', color: 'text-blue-500 bg-blue-500/10' },
  { title: '已完成任务', value: '0', unit: '个', icon: 'IconCheckCircle', color: 'text-green-500 bg-green-500/10' },
  { title: '失败任务', value: '0', unit: '个', icon: 'IconExclamationCircle', color: 'text-red-500 bg-red-500/10' },
]);

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
  return map[status] || 'gray';
};

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

const hasSignedIn = ref(false);
const totalSignedDays = ref(0);
const signedDates = ref<string[]>([]);

const weeklyDays = ref<any[]>([]);

const initWeeklyDays = (signedList: string[]) => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const today = new Date();
  const currentDay = today.getDay(); // 0 is Sunday
  const diff = today.getDate() - (currentDay === 0 ? 6 : currentDay - 1); // Adjust to Monday
  const monday = new Date(today);
  monday.setDate(diff);

  const week = [];
  for (let i = 0; i < 7; i++) {
    const date = new Date(monday);
    date.setDate(monday.getDate() + i);
    const dateStr = date.toISOString().split('T')[0];
    const isTodayDate = dateStr === new Date().toISOString().split('T')[0];
    week.push({
      label: days[i],
      date: dateStr,
      signed: signedList.includes(dateStr),
      isToday: isTodayDate,
      points: '+5'
    });
  }
  weeklyDays.value = week;
};

const fetchSignInStatus = async () => {
  try {
    const res: any = await getMonthSignInDays();
    const list = res.data || [];
    signedDates.value = list;
    totalSignedDays.value = list.length;
    
    const todayStr = new Date().toISOString().split('T')[0];
    hasSignedIn.value = list.includes(todayStr);
    
    initWeeklyDays(list);
  } catch (error) {
    console.error('获取签到状态失败', error);
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    const [points, tasks, statistics]: any = await Promise.all([
      getPointsBalance(),
      getTaskList({ pageNum: 1, pageSize: 5 }), 
      getTaskStatistics(),
      fetchSignInStatus() // 同步获取签到状态
    ]);
    
    const balance = typeof points.data === 'object' ? points.data.balance : points.data;
    pointsBalance.value = balance || 0;
    
    latestTasks.value = tasks.data.records || tasks.data.list || [];
    
    const statsData = statistics.data;
    if (stats.value[0]) stats.value[0].value = pointsBalance.value.toLocaleString();
    if (stats.value[1]) stats.value[1].value = statsData.inProgressCount.toString();
    if (stats.value[2]) stats.value[2].value = statsData.completedCount.toString();
    if (stats.value[3]) stats.value[3].value = statsData.failedCount.toString();
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleSignIn = async () => {
  try {
    const res: any = await signIn();
    Message.success(res.msg || '签到成功！积分 +5');
    // 签到成功后重新拉取完整状态，确保 UI 数据一致性（包括周进度、累计天数）
    fetchSignInStatus();
    // 同时也刷新基础数据（如积分余额）
    fetchData();
  } catch (error) {}
};

const fetchRecords = async () => {
  recordsLoading.value = true;
  try {
    const res: any = await getPointsRecords({ pageNum: 1, pageSize: 20 });
    records.value = res.data.records || res.data.list || [];
    showRecords.value = true;
  } catch (error) {} finally {
    recordsLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.dashboard-container {
  padding: 12px 0;
}

.mg-text-gradient {
  background: linear-gradient(135deg, var(--mg-primary) 0%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stats-card {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.icon-box {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-item {
  margin: 4px 0;
}

:deep(.arco-card-header) {
  border-bottom: 1px solid var(--mg-border);
  padding: 16px 20px;
}

:deep(.arco-card-header-title) {
  font-weight: 700;
  font-size: 16px;
}

/* Page & Component Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
