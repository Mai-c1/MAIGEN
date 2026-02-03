<template>
  <div class="community-detail-container max-w-5xl mx-auto">
    <a-breadcrumb class="mb-6">
      <a-breadcrumb-item @click="router.push('/community')" class="cursor-pointer">社区广场</a-breadcrumb-item>
      <a-breadcrumb-item>题目详情</a-breadcrumb-item>
    </a-breadcrumb>

    <div v-if="loading" class="flex justify-center py-20">
      <a-spin :size="40" />
    </div>

    <template v-else-if="content">
      <a-row :gutter="24">
        <!-- 左侧主体 -->
        <a-col :span="16">
          <a-card :bordered="false" class="bg-bg-card mb-6">
            <div class="flex justify-between items-start mb-6">
              <div>
                <a-typography-title :heading="2" class="!m-0">{{ content.title }}</a-typography-title>
                <div class="flex items-center gap-4 mt-2 text-gray-400 text-sm">
                  <span><icon-user class="mr-1" />{{ content.authorName || '匿名用户' }}</span>
                  <span><icon-calendar class="mr-1" />{{ content.createdAt }}</span>
                  <span><icon-eye class="mr-1" />{{ content.downloadCount }} 下载</span>
                </div>
              </div>
              <a-space>
                <a-button type="outline" @click="handleLike">
                  <template #icon><icon-heart :fill="content.isLiked ? '#ff4d4f' : 'none'" /></template>
                  {{ content.likeCount }}
                </a-button>
                <a-button type="primary" @click="handleDownload">
                  积分解锁 ({{ content.points }} MAI)
                </a-button>
              </a-space>
            </div>

            <a-tabs default-active-key="desc">
              <a-tab-pane key="desc" title="题目描述">
                <div class="prose prose-invert max-w-none py-4">
                  <pre class="whitespace-pre-wrap font-sans text-base leading-relaxed text-gray-300">{{ content.problemDescription }}</pre>
                </div>
              </a-tab-pane>
              <a-tab-pane key="code" title="参考代码">
                <div class="py-4">
                  <CodeEditor 
                    :model-value="content.standardCode || ''" 
                    :read-only="true"
                    height="400px"
                  />
                </div>
              </a-tab-pane>
            </a-tabs>
          </a-card>

          <!-- 评论/评分区 (预留) -->
          <a-card title="评价反馈" :bordered="false" class="bg-bg-card">
            <div class="flex items-center gap-8 py-4">
              <div class="text-center">
                <div class="text-4xl font-bold text-primary">{{ content.ratingAvg || '0.0' }}</div>
                <div class="text-gray-500 text-xs mt-1">综合评分</div>
              </div>
              <div class="flex-1">
                <div class="text-gray-400 text-sm mb-2">为您对此资源的满意度评分：</div>
                <a-rate v-model="userRate" allow-half @change="handleRate" />
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧边栏 -->
        <a-col :span="8">
          <a-card title="资源信息" :bordered="false" class="bg-bg-card mb-6">
            <div class="space-y-4">
              <div class="flex justify-between">
                <span class="text-gray-400">资源分类</span>
                <a-tag color="arcoblue">{{ content.categoryName }}</a-tag>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-400">标签</span>
                <a-space size="mini">
                  <a-tag v-for="tag in content.tags" :key="tag" size="small">{{ tag }}</a-tag>
                </a-space>
              </div>
            </div>
          </a-card>

          <a-card title="作者其他资源" :bordered="false" class="bg-bg-card">
            <div class="py-10 text-center text-gray-500 italic">暂无更多</div>
          </a-card>
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { 
  IconUser, 
  IconCalendar, 
  IconEye, 
  IconHeart
} from '@arco-design/web-vue/es/icon';
import CodeEditor from '@/components/CodeEditor.vue';
import { getCommunityDetail, downloadResource, likeContent, rateContent } from '@/api/community';

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const content = ref<any>(null);
const userRate = ref(0);

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res: any = await getCommunityDetail(route.params.id as string);
    content.value = res.data;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleLike = async () => {
  try {
    await likeContent(content.value.id);
    content.value.likeCount++;
    content.value.isLiked = true;
    Message.success('点赞成功');
  } catch (error) {
    console.error(error);
  }
};

const handleDownload = () => {
  Modal.confirm({
    title: '确认解锁',
    content: `解锁此资源将消耗 ${content.value.points} MAI 积分，确认继续吗？`,
    onOk: async () => {
      try {
        const res: any = await downloadResource(content.value.id);
        Message.success('解锁成功！');
        if (res.data) window.open(res.data);
      } catch (error) {
        console.error(error);
      }
    }
  });
};

const handleRate = async (val: number) => {
  try {
    await rateContent({ contentId: content.value.id, score: val });
    Message.success('评分成功');
  } catch (error) {
    console.error(error);
  }
};

onMounted(fetchDetail);
</script>

<style scoped>
.community-detail-container {
  padding-bottom: 40px;
}
</style>
