<template>
  <div class="community-container max-w-4xl mx-auto py-6">
    <!-- 顶部发布区 (Mock) -->
    <a-card :bordered="false" class="mb-6 post-card">
      <div class="flex gap-4">
        <a-avatar :size="48" class="flex-shrink-0">
          <img src="https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f1341c7918341.png~tplv-uwbnlip3yd-webp.webp" />
        </a-avatar>
        <div class="flex-1">
          <a-textarea
            placeholder="分享你的算法见解或新生成的题目数据集..."
            :auto-size="{ minRows: 2, maxRows: 5 }"
            class="mb-3 custom-textarea"
          />
          <div class="flex justify-between items-center">
            <a-space>
              <a-button type="text" size="small"><template #icon><icon-image /></template></a-button>
              <a-button type="text" size="small"><template #icon><icon-code-square /></template></a-button>
              <a-button type="text" size="small"><template #icon><icon-face-smile-fill /></template></a-button>
            </a-space>
            <a-button type="primary" shape="round">发布</a-button>
          </div>
        </div>
      </div>
    </a-card>

    <!-- 分类标签栏 -->
    <div class="flex gap-2 mb-6 overflow-x-auto pb-2 scrollbar-hide">
      <a-tag
        v-for="tag in tags"
        :key="tag"
        checkable
        :checked="activeTag === tag"
        @check="activeTag = tag"
        size="large"
        class="cursor-pointer rounded-full px-6"
        :color="activeTag === tag ? 'arcoblue' : 'gray'"
      >
        {{ tag }}
      </a-tag>
    </div>

    <!-- 动态内容流 -->
    <a-space direction="vertical" fill size="large" :loading="loading">
      <div v-for="post in postList" :key="post.id" class="post-item bg-bg-card p-5 rounded-xl border border-transparent hover:border-primary/30 transition-all cursor-pointer" @click="router.push(`/community/detail/${post.id}`)">
        <div class="flex gap-4">
          <a-avatar :size="48" class="flex-shrink-0">
            <img :src="post.authorAvatar || 'https://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/3ee5f1341c7918341.png~tplv-uwbnlip3yd-webp.webp'" />
          </a-avatar>
          <div class="flex-1">
            <div class="flex justify-between items-center mb-1">
              <div class="flex items-center gap-2">
                <span class="font-bold text-[var(--mg-text-1)] text-lg">{{ post.authorName }}</span>
                <span class="text-[var(--mg-text-3)] text-sm">@{{ post.authorName }} · {{ post.createdAt }}</span>
              </div>
              <a-dropdown @click.stop>
                <icon-more class="text-[var(--mg-text-3)] cursor-pointer" />
                <template #content>
                  <a-doption>不感兴趣</a-doption>
                  <a-doption>举报</a-doption>
                </template>
              </a-dropdown>
            </div>
            <p class="text-[var(--mg-text-2)] text-base leading-relaxed mb-3">
              {{ post.title }} - {{ post.categoryName }}
            </p>
            <!-- 资源预览卡片 -->
            <div class="resource-preview mb-4 p-4 rounded-lg border border-[var(--mg-text-3)]/10 bg-black/5 flex justify-between items-center">
              <div class="flex items-center gap-3">
                <div class="p-2 bg-primary/10 rounded">
                  <icon-file-pdf class="text-primary text-xl" />
                </div>
                <div>
                  <div class="text-[var(--mg-text-1)] font-medium">{{ post.title }}.zip</div>
                <div class="text-[var(--mg-text-3)] text-xs">{{ post.downloadCount }} 次下载 · {{ post.ratingAvg }} 分</div>
              </div>
            </div>
            <a-button type="primary" size="small" shape="round" @click.stop="handleDownload(post)">
              积分解锁 ({{ post.points }} MAI)
            </a-button>
          </div>
          <!-- 互动操作栏 -->
          <div class="flex justify-between max-w-sm text-gray-500">
            <span class="flex items-center gap-2 hover:text-blue-400 transition-colors cursor-pointer group" @click.stop>
              <div class="p-2 rounded-full group-hover:bg-blue-400/10"><icon-message /></div>
              0
            </span>
            <span class="flex items-center gap-2 hover:text-green-400 transition-colors cursor-pointer group" @click.stop>
              <div class="p-2 rounded-full group-hover:bg-green-400/10"><icon-sync /></div>
              {{ post.downloadCount }}
            </span>
            <span class="flex items-center gap-2 hover:text-red-400 transition-colors cursor-pointer group" @click.stop="handleLike(post)">
              <div class="p-2 rounded-full group-hover:bg-red-400/10"><icon-heart /></div>
              {{ post.likeCount }}
            </span>
            <span class="flex items-center gap-2 hover:text-blue-400 transition-colors cursor-pointer group" @click.stop>
              <div class="p-2 rounded-full group-hover:bg-blue-400/10"><icon-share-alt /></div>
            </span>
          </div>
          </div>
        </div>
      </div>
      <template v-if="postList.length === 0 && !loading">
        <div class="py-20 text-center text-gray-500">暂无社区动态</div>
      </template>
    </a-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Modal } from '@arco-design/web-vue';
import { 
  IconImage, 
  IconCodeSquare, 
  IconFaceSmileFill, 
  IconMore, 
  IconFilePdf, 
  IconMessage, 
  IconSync, 
  IconHeart, 
  IconShareAlt 
} from '@arco-design/web-vue/es/icon';
import { getCommunityList, getCategories, getTags, downloadResource, likeContent } from '@/api/community';

const router = useRouter();
const activeTag = ref('推荐');
const tags = ref<string[]>(['推荐', '最新']);
const categories = ref<any[]>([]);
const postList = ref<any[]>([]);
const loading = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const [posts, catRes, tagRes]: any = await Promise.all([
      getCommunityList({ pageNum: 1, pageSize: 10 }),
      getCategories(),
      getTags()
    ]);
    postList.value = posts.data.records || posts.data.list || [];
    categories.value = catRes.data || [];
    // 合并标签
    if (tagRes && tagRes.data) {
      tags.value = ['推荐', '最新', ...tagRes.data.map((t: any) => t.name)];
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleDownload = async (post: any) => {
  Modal.confirm({
    title: '确认解锁',
    content: `解锁此资源将消耗 ${post.points} MAI 积分，确认继续吗？`,
    okText: '确认解锁',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res: any = await downloadResource(post.id);
        Message.success('解锁成功！');
        if (res.data) {
          window.open(res.data);
        }
      } catch (error) {
        console.error(error);
      }
    }
  });
};

const handleLike = async (post: any) => {
  try {
    await likeContent(post.id);
    post.likeCount++;
    Message.success('点赞成功');
  } catch (error) {
    console.error(error);
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.community-container {
  padding-bottom: 100px;
}

.post-card {
  background: var(--color-bg-2);
  border-radius: 16px;
}

.custom-textarea :deep(.arco-textarea) {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  color: #fff;
}

.custom-textarea :deep(.arco-textarea):focus {
  background: transparent;
  box-shadow: none;
}

.post-item {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.resource-preview {
  transition: background-color 0.2s;
}

.resource-preview:hover {
  background-color: rgba(22, 93, 255, 0.05);
}
</style>
