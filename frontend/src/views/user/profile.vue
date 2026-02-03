<template>
  <div class="profile-container max-w-5xl mx-auto">
    <!-- 顶部个人资料卡片 -->
    <div class="profile-header-card glass-card p-8 rounded-[32px] mb-8 relative overflow-hidden">
      <div class="absolute -right-20 -top-20 w-80 h-80 bg-primary/10 rounded-full blur-3xl animate-pulse"></div>
      <div class="absolute -left-10 -bottom-10 w-40 h-40 bg-blue-500/5 rounded-full blur-2xl"></div>
      
      <div class="flex flex-col md:flex-row items-center gap-8 relative z-10">
        <div class="relative group">
          <a-avatar :size="120" class="shadow-2xl border-4 border-[var(--mg-border)] ring-4 ring-primary/20 transition-transform group-hover:scale-105 duration-500">
            <img 
              v-if="userForm.avatar"
              :key="userForm.avatar"
              :src="userInfo.avatar"
              style="object-fit: cover;"
            />
          </a-avatar>
          <a-upload
            action="/api/common/upload"
            :show-file-list="false"
            @success="handleAvatarSuccess"
            class="absolute bottom-1 right-1"
          >
            <template #upload-button>
              <div class="p-2 bg-primary rounded-full shadow-lg cursor-pointer hover:scale-110 transition-transform">
                <icon-camera class="text-white text-lg" />
              </div>
            </template>
          </a-upload>
        </div>
        
        <div class="flex-1 text-center md:text-left">
          <div class="flex flex-col md:flex-row md:items-center gap-3 mb-2">
            <h1 class="text-3xl font-black text-[var(--mg-text-1)] m-0 tracking-tight">
              {{ userForm.nickname || '未设置昵称' }}
            </h1>
            <a-tag color="arcoblue" class="rounded-full px-4 font-bold self-center md:self-auto">
              ID: {{ userInfo?.id || '---' }}
            </a-tag>
          </div>
          <p class="text-[var(--mg-text-3)] text-lg mb-4 font-medium">{{ userInfo?.email || '暂无邮箱信息' }}</p>
          <p class="text-[var(--mg-text-2)] text-sm mb-6 max-w-2xl">{{ userForm.bio }}</p>
          
          <div class="flex flex-wrap justify-center md:justify-start gap-4">
            <div class="info-badge">
              <span class="label">加入时间</span>
              <span class="value">{{ formatDate(userInfo?.createdAt) }}</span>
            </div>
            <div class="info-badge group cursor-pointer" @click="copyInviteCode">
              <span class="label">邀请码</span>
              <span class="value flex items-center gap-1">
                {{ userInfo?.invitationCode || '---' }}
                <icon-copy class="opacity-0 group-hover:opacity-100 transition-opacity" />
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <a-row :gutter="24">
      <!-- 左侧内容区 -->
      <a-col :span="16">
        <a-tabs default-active-key="profile" class="custom-tabs glass-card p-6 rounded-3xl mb-6 min-h-[500px]">
          <!-- 1. 个人资料 -->
          <a-tab-pane key="profile" title="个人资料">
            <div class="mt-4 px-2">
              <a-form :model="userForm" layout="vertical" @submit="handleUpdateProfile">
                <a-row :gutter="20">
                  <a-col :span="12">
                    <a-form-item label="显示昵称" required>
                      <a-input v-model="userForm.nickname" placeholder="设置你的昵称" size="large" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="用户名 (唯一标识)">
                      <a-input :model-value="userInfo?.username" disabled size="large" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)] opacity-60" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item label="头像 URL">
                  <a-input v-model="userForm.avatar" placeholder="输入头像 URL 或通过上方按钮上传" size="large" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
                </a-form-item>
                <a-form-item label="个人简介">
                  <a-textarea v-model="userForm.bio" placeholder="向大家介绍一下自己吧" :auto-size="{ minRows: 3 }" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
                </a-form-item>
                <div class="flex justify-end mt-4">
                  <a-button type="primary" :loading="updating" html-type="submit" size="large" class="rounded-xl px-8 shadow-lg shadow-primary/20">
                    保存基本资料
                  </a-button>
                </div>
              </a-form>
            </div>
          </a-tab-pane>

          <!-- 2. 积分记录 -->
          <a-tab-pane key="points" title="积分记录">
            <div class="mt-4">
              <a-table 
                :data="pointsRecords" 
                :loading="loadingPoints" 
                :pagination="pointsPagination"
                @page-change="onPointsPageChange"
                class="custom-table" 
                :bordered="false"
              >
                <template #columns>
                  <a-table-column title="变动积分" width="100">
                    <template #cell="{ record }">
                      <span :class="record.amount > 0 ? 'text-green-500 font-black' : 'text-red-500 font-black'">
                        {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
                      </span>
                    </template>
                  </a-table-column>
                  <a-table-column title="类型" data-index="source" width="120">
                    <template #cell="{ record }">
                      <a-tag size="small">{{ getSourceText(record.source) }}</a-tag>
                    </template>
                  </a-table-column>
                  <a-table-column title="详细描述" data-index="description" />
                  <a-table-column title="时间" data-index="createdAt" width="180">
                    <template #cell="{ record }">
                      {{ formatTime(record.createdAt) }}
                    </template>
                  </a-table-column>
                </template>
              </a-table>
            </div>
          </a-tab-pane>

          <!-- 3. 安全设置 -->
          <a-tab-pane key="security" title="安全设置">
            <div class="mt-4 px-2">
              <div class="mb-8 p-4 bg-orange-500/5 border border-orange-500/20 rounded-2xl flex items-start gap-3">
                <icon-info-circle-fill class="text-orange-500 mt-1" />
                <div>
                  <div class="text-orange-500 font-bold mb-1">定期修改密码</div>
                  <div class="text-[var(--mg-text-3)] text-xs">为了您的账户安全，建议每 3-6 个月更换一次登录密码，并避免使用与其他网站相同的密码。</div>
                </div>
              </div>
              <a-form :model="passwordForm" layout="vertical" @submit="handleChangePassword">
                <a-form-item label="当前登录密码" required>
                  <a-input-password v-model="passwordForm.oldPassword" placeholder="请输入当前正在使用的密码" size="large" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
                </a-form-item>
                <a-row :gutter="20">
                  <a-col :span="12">
                    <a-form-item label="设置新密码" required>
                      <a-input-password v-model="passwordForm.newPassword" placeholder="建议包含字母与数字" size="large" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="确认新密码" required>
                      <a-input-password v-model="passwordForm.confirmPassword" placeholder="再次输入新密码" size="large" class="rounded-xl bg-[var(--mg-bg-1)] border-[var(--mg-border)]" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <div class="flex justify-end mt-4">
                  <a-button type="primary" status="danger" :loading="changing" html-type="submit" size="large" class="rounded-xl px-8 shadow-lg shadow-red-500/20">
                    确认修改密码
                  </a-button>
                </div>
              </a-form>
            </div>
          </a-tab-pane>
        </a-tabs>
      </a-col>

      <!-- 右侧侧边栏 -->
      <a-col :span="8">
        <!-- 签到 -->
        <a-card title="每日签到" :bordered="false" class="mb-6 glass-card rounded-3xl">
          <div class="text-center py-2">
            <div class="mb-4">
              <div class="text-4xl mb-2">🎁</div>
              <div class="text-[var(--mg-text-2)] font-bold">今日签到可领 5 积分</div>
            </div>
            <a-button type="primary" size="large" long class="rounded-xl shadow-lg shadow-primary/20" :disabled="hasSignedIn" @click="handleSignIn">
              {{ hasSignedIn ? '今日已签到' : '立即签到' }}
            </a-button>
          </div>
        </a-card>

        <!-- 统计 -->
        <a-card title="任务统计" :bordered="false" class="mb-6 glass-card rounded-3xl">
          <div class="grid grid-cols-2 gap-4">
            <div class="stat-box">
              <div class="label">总任务</div>
              <div class="value">{{ stats?.totalCount || 0 }}</div>
            </div>
            <div class="stat-box">
              <div class="label text-green-500">已完成</div>
              <div class="value">{{ stats?.completedCount || 0 }}</div>
            </div>
            <div class="stat-box">
              <div class="label text-blue-500">进行中</div>
              <div class="value">{{ stats?.inProgressCount || 0 }}</div>
            </div>
            <div class="stat-box">
              <div class="label text-red-500">已失败</div>
              <div class="value">{{ stats?.failedCount || 0 }}</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { IconCamera, IconCopy, IconExport, IconCommon, IconCalendar, IconCheckCircleFill, IconInfoCircleFill } from '@arco-design/web-vue/es/icon';
import { useUserStore } from '@/store/user';
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user';
import { getPointsRecords, signIn, getMonthSignInDays } from '@/api/points';
import { getTaskStatistics } from '@/api/task';

const router = useRouter();
const userStore = useUserStore();
const updating = ref(false);
const changing = ref(false);
const loadingPoints = ref(false);
const userInfo = ref<any>(null);
const pointsRecords = ref<any[]>([]);
const stats = ref<any>(null);
const hasSignedIn = ref(false);

const userForm = ref({
  nickname: '',
  avatar: '',
  bio: '',
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// 积分分页
const pointsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showJumper: true,
});

const fetchData = async () => {
  try {
    const [infoRes, statsRes]: any = await Promise.all([
      getUserInfo(),
      getTaskStatistics(),
    ]);
    console.log(infoRes.data)
    userInfo.value = infoRes.data;
    userForm.value.nickname = infoRes.data.nickname;
    userForm.value.avatar = infoRes.data.avatar;
    userForm.value.bio = infoRes.data.bio || '算法竞赛爱好者，MAIGEN 深度用户。';
    stats.value = statsRes.data;
    userStore.setUserInfo(infoRes.data);
    
    loadPointsRecords();
    checkSignInStatus();
  } catch (error) {
    console.error(error);
  }
};

const loadPointsRecords = async () => {
  loadingPoints.value = true;
  try {
    const res: any = await getPointsRecords({ 
      page: pointsPagination.current, 
      size: pointsPagination.pageSize 
    });
    pointsRecords.value = res.data.records || [];
    pointsPagination.total = Number(res.data.total) || 0;
  } catch (error) {
    console.error('Load points failed:', error);
  }
  finally { loadingPoints.value = false; }
};

const onPointsPageChange = (page: number) => {
  pointsPagination.current = page;
  loadPointsRecords();
};

const checkSignInStatus = async () => {
  try {
    const res: any = await getMonthSignInDays();
    const today = new Date().toISOString().split('T')[0];
    hasSignedIn.value = (res.data || []).includes(today);
  } catch (error) {}
};

const handleSignIn = async () => {
  try {
    const res: any = await signIn();
    Message.success(res.msg || '签到成功');
    hasSignedIn.value = true;
    fetchData();
  } catch (error) {}
};

const handleAvatarSuccess = (fileItem: any) => {
  // 假设后端直接返回图片URL
  const url = fileItem.response.data;
  userForm.value.avatar = url;
  Message.success('头像上传成功');
};


const getSourceText = (source: string) => {
  const map: Record<string, string> = {
    'SIGN_IN': '每日签到',
    'TASK_CREATE': '创建任务',
    'TASK_REFUND': '任务退还',
    'SHARE_REWARD': '分享奖励',
    'EXCHANGE': '积分兑换'
  };
  return map[source] || source;
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '---';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '---';
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

const copyInviteCode = () => {
  if (userInfo.value?.invitationCode) {
    navigator.clipboard.writeText(userInfo.value.invitationCode);
    Message.success('邀请码已复制到剪贴板');
  }
};

const handleUpdateProfile = async () => {
  if (!userForm.value.nickname) {
    Message.warning('昵称不能为空');
    return;
  }
  updating.value = true;
  try {
    // 只发送昵称、头像、简介
    const updateData = {
      nickname: userForm.value.nickname,
      avatar: userForm.value.avatar,
      bio: userForm.value.bio
    };
    const res = await updateUserInfo(updateData);
    Message.success(res.msg || '个人资料更新成功');
    fetchData();
  } catch (error) {
    console.error(error);
  } finally {
    updating.value = false;
  }
};

const handleChangePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value;
  if (!oldPassword || !newPassword || !confirmPassword) {
    Message.warning('请填写完整密码信息');
    return;
  }
  if (newPassword !== confirmPassword) {
    Message.error('两次输入的新密码不一致');
    return;
  }
  changing.value = true;
  try {
    const res = await changePassword({ oldPassword, newPassword });
    Message.success(res.msg || '密码修改成功，请重新登录');
    userStore.logout();
    router.push('/login');
  } catch (error) {
    console.error(error);
  } finally {
    changing.value = false;
  }
};

onMounted(fetchData);
</script>

<style scoped>
.profile-container {
  padding-bottom: 60px;
}

.info-badge {
  background: var(--mg-bg-1);
  border: 1px solid var(--mg-border);
  padding: 6px 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}

.info-badge .label {
  font-size: 10px;
  color: var(--mg-text-3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: bold;
}

.info-badge .value {
  font-size: 14px;
  color: var(--mg-text-1);
  font-weight: 600;
}

.stat-box {
  background: var(--mg-bg-1);
  border: 1px solid var(--mg-border);
  padding: 16px;
  border-radius: 20px;
  text-align: center;
}

.stat-box .label {
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 4px;
  opacity: 0.8;
}

.stat-box .value {
  font-size: 24px;
  font-weight: 900;
  color: var(--mg-text-1);
}

:deep(.arco-form-item-label) {
  color: var(--mg-text-2);
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 8px;
}

:deep(.arco-card-header) {
  border-bottom: 1px solid var(--mg-border);
  padding: 20px 24px;
}

:deep(.arco-card-header-title) {
  font-weight: 800;
  font-size: 15px;
  color: var(--mg-text-1);
}

.custom-table :deep(.arco-table-th) {
  background: transparent;
  color: var(--mg-text-3);
  font-size: 12px;
  font-weight: bold;
  border-bottom: 1px solid var(--mg-border);
}

.custom-table :deep(.arco-table-td) {
  background: transparent;
  border-bottom: 1px solid var(--mg-border);
  opacity: 0.8;
}
</style>
