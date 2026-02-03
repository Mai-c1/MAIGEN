<template>
  <div class="task-create-wizard">
    <!-- 顶部步骤条 -->
    <div class="wizard-header glass-card mb-8 p-8 rounded-3xl">
      <div class="flex items-center gap-4 mb-8">
        <div class="p-3 bg-primary/10 rounded-2xl text-primary">
          <icon-experiment :style="{ fontSize: '32px' }" />
        </div>
        <div>
          <h1 class="text-2xl font-bold text-[var(--mg-text-1)] m-0">创建生成任务</h1>
          <p class="text-[var(--mg-text-3)] m-0">只需三步，AI 将为您构建完美的竞赛题目</p>
        </div>
      </div>
      <a-steps :current="currentStep" line-less class="custom-steps">
        <a-step title="基础定义" description="配置题目基本参数" />
        <a-step title="内容创作" description="编写题目描述与解法" />
        <a-step title="确认发布" description="费用结算与任务提交" />
      </a-steps>
    </div>

    <!-- 步骤内容区 -->
    <div class="wizard-content min-h-[500px]">
      <transition name="fade" mode="out-in">
        <!-- 第一步：基础定义 -->
        <div v-if="currentStep === 1" key="step1" class="step-container">
          <a-row :gutter="24" class="mb-6">
            <!-- 题目名称 -->
            <a-col :span="10">
              <a-card class="glass-card h-full" title="题目定义" :bordered="false">
                <a-form :model="form" layout="vertical">
                  <a-form-item label="题目名称" required>
                    <a-input v-model="form.title" placeholder="如：最长公共子序列 (LCS)" size="large" class="rounded-xl" />
                  </a-form-item>
                  <div class="mt-4 p-4 bg-primary/5 rounded-xl border border-primary/10">
                    <div class="text-xs text-primary font-bold mb-1 flex items-center gap-1">
                      <icon-bulb /> 建议
                    </div>
                    <div class="text-xs text-[var(--mg-text-3)]">名称应简明，方便 AI 理解算法类型</div>
                  </div>
                </a-form>
              </a-card>
            </a-col>
            <!-- 规格配置 -->
            <a-col :span="14">
              <a-card class="glass-card h-full" title="规格配置" :bordered="false">
                <a-form :model="form" layout="vertical">
                  <a-row :gutter="20">
                    <a-col :span="8">
                      <a-form-item label="时间限制 (ms)" required>
                        <a-input-number v-model="form.timeLimit" :min="100" :max="10000" step="100" size="large" class="w-full rounded-xl" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="8">
                      <a-form-item label="内存限制 (MB)" required>
                        <a-input-number v-model="form.memoryLimit" :min="64" :max="1024" step="64" size="large" class="w-full rounded-xl" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="8">
                      <a-form-item label="用例数量" required>
                        <a-input-number v-model="form.targetCases" :min="1" :max="50" size="large" class="w-full rounded-xl" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                  <a-form-item label="用例分布强度" class="mb-0">
                    <a-slider v-model="form.targetCases" :min="1" :max="50" />
                  </a-form-item>
                </a-form>
              </a-card>
            </a-col>
          </a-row>

          <!-- 生成策略 -->
          <a-card class="glass-card" title="生成策略 (多选)" :bordered="false">
            <template #extra>
              <a-link @click="form.strategyIds = strategies.map(s => s.id)">全选</a-link>
            </template>
            <div class="strategy-grid">
              <div 
                v-for="item in strategies" 
                :key="item.id"
                class="strategy-card"
                :class="{ 'active': form.strategyIds.includes(item.id) }"
                @click="toggleStrategy(item.id)"
              >
                <div class="strategy-icon">
                  <component :is="getStrategyIcon(item.name)" />
                </div>
                <div class="strategy-info">
                  <div class="strategy-name">{{ item.name }}</div>
                  <div class="strategy-desc">{{ item.description }}</div>
                </div>
                <div class="strategy-checkbox">
                  <icon-check-circle-fill v-if="form.strategyIds.includes(item.id)" />
                  <div v-else class="checkbox-placeholder"></div>
                </div>
              </div>
            </div>
            
            <div class="mt-8 flex items-center justify-center gap-8 border-t border-white/5 pt-6">
              <div class="flex items-center gap-2 text-[var(--mg-text-3)] text-sm">
                <icon-check-circle class="text-green-500" /> 多样性覆盖
              </div>
              <div class="flex items-center gap-2 text-[var(--mg-text-3)] text-sm">
                <icon-check-circle class="text-green-500" /> 边界强度保障
              </div>
              <div class="flex items-center gap-2 text-[var(--mg-text-3)] text-sm">
                <icon-check-circle class="text-green-500" /> 算法合规性检查
              </div>
            </div>
          </a-card>
        </div>

        <!-- 第二步：内容创作 -->
        <div v-else-if="currentStep === 2" key="step2" class="step-container">
          <div class="space-y-6">
            <a-card class="glass-card" :bordered="false">
              <template #title>
                <div class="flex justify-between items-center">
                  <div class="flex items-center gap-2">
                    <icon-file-pdf /> 题目描述 (Markdown)
                  </div>
                  <a-tag size="small">支持 LaTeX 公式</a-tag>
                </div>
              </template>
              <div class="editor-wrapper">
                <MgMdEditor v-model="form.description" style="height: 400px" />
              </div>
            </a-card>

            <a-card class="glass-card" :bordered="false">
              <template #title>
                <div class="flex justify-between items-center">
                  <div class="flex items-center gap-2">
                    <icon-code /> 标准解法 (C++)
                  </div>
                  <a-tag size="small" color="green">C++ 17</a-tag>
                </div>
              </template>
              <div class="editor-wrapper">
                <CodeEditor v-model="form.standardCode" mode="c_cpp" :theme="aceTheme" style="height: 500px" />
              </div>
            </a-card>
          </div>
        </div>

        <!-- 第三步：确认发布 -->
        <div v-else key="step3" class="step-container max-w-2xl mx-auto">
          <a-card class="glass-card text-center p-8" :bordered="false">
            <div class="mb-8">
              <icon-check-circle-fill class="text-green-500 text-6xl mb-4" />
              <h2 class="text-2xl font-bold text-[var(--mg-text-1)]">配置已就绪</h2>
              <p class="text-[var(--mg-text-2)]">请核对以下核心参数，确认无误后提交</p>
            </div>
            
            <div class="bg-white/5 rounded-2xl p-6 text-left space-y-4 mb-8">
              <div class="flex justify-between border-b border-white/5 pb-2">
                <span class="text-[var(--mg-text-3)]">题目名称</span>
                <span class="text-[var(--mg-text-1)] font-medium">{{ form.title }}</span>
              </div>
              <div class="flex justify-between border-b border-white/5 pb-2">
                <span class="text-[var(--mg-text-3)]">测试策略</span>
                <div class="flex flex-wrap gap-2 justify-end max-w-[70%]">
                  <a-tag v-for="id in form.strategyIds" :key="id" color="arcoblue" size="small" class="rounded-md">
                    <template #icon><component :is="getStrategyIcon(strategies.find(s => s.id === id)?.name)" /></template>
                    {{ strategies.find(s => s.id === id)?.name }}
                  </a-tag>
                </div>
              </div>
              <div class="flex justify-between border-b border-white/5 pb-2">
                <span class="text-[var(--mg-text-3)]">测试点数量</span>
                <span class="text-[var(--mg-text-1)]">{{ form.targetCases }} 个</span>
              </div>
              <div class="flex justify-between pt-2">
                <span class="text-[var(--mg-text-2)] font-bold">任务消耗</span>
                <span class="text-primary text-2xl font-extrabold">5 MAI</span>
              </div>
            </div>

            <div class="flex items-center gap-3 p-4 bg-primary/10 rounded-xl text-primary text-sm mb-8">
              <icon-info-circle />
              <span>任务提交后，AI 预计将在 1-3 分钟内完成数据生成。</span>
            </div>
          </a-card>
        </div>
      </transition>
    </div>

    <!-- 底部操作栏 -->
    <div class="wizard-footer mt-8 flex justify-between items-center">
      <a-button v-if="currentStep > 1" size="large" @click="currentStep--" class="rounded-xl px-8">
        上一步
      </a-button>
      <div v-else></div>
      
      <a-space size="large">
        <a-button size="large" @click="handleReset" class="rounded-xl">重置</a-button>
        <a-button 
          v-if="currentStep < 3" 
          type="primary" 
          size="large" 
          @click="nextStep" 
          class="rounded-xl px-12"
        >
          下一步
        </a-button>
        <a-button 
          v-else 
          type="primary" 
          size="large" 
          :loading="submitting" 
          @click="handleSubmit" 
          class="rounded-xl px-12"
        >
          确认并开始生成
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { 
  IconExperiment, 
  IconFilePdf, 
  IconCode, 
  IconCheckCircle,
  IconBulb,
  IconCheckCircleFill,
  IconInfoCircle,
  IconPlus,
  IconApps,
  IconSafe,
  IconThunderbolt,
  IconCommand,
  IconBranch,
  IconStorage,
  IconBug,
  IconLayers
} from '@arco-design/web-vue/es/icon';
import CodeEditor from '@/components/CodeEditor.vue';
import MgMdEditor from '@/components/MgMdEditor.vue';
import { createTask, getStrategies } from '@/api/task';
import { useAppStore } from '@/store/app';

const router = useRouter();
const appStore = useAppStore();
const currentStep = ref(1);

const aceTheme = computed(() => appStore.theme === 'dark' ? 'tomorrow_night' : 'github');
const submitting = ref(false);
const strategies = ref<any[]>([]);

const iconMap: Record<string, any> = {
  '基础随机': IconApps,
  '边界极值': IconSafe,
  '顺序特征': IconThunderbolt,
  '复杂度边界': IconCommand,
  '特殊结构': IconBranch,
  '数据分布': IconStorage,
  '对抗性': IconBug,
  '组合特征': IconLayers
};

const getStrategyIcon = (name: string) => {
  return iconMap[name] || IconApps;
};

const toggleStrategy = (id: number) => {
  const index = form.value.strategyIds.indexOf(id);
  if (index > -1) {
    if (form.value.strategyIds.length > 1) {
      form.value.strategyIds.splice(index, 1);
    } else {
      Message.warning('请至少保留一种测试策略');
    }
  } else {
    form.value.strategyIds.push(id);
  }
};

const STORAGE_KEY = 'maigen_task_create_form';

const form = ref({
  title: '',
  timeLimit: 1000,
  memoryLimit: 256,
  targetCases: 10,
  strategyIds: [] as number[],
  description: '# 题目描述\n\n## 描述\n\n请在这里输入题目的 Markdown 描述...\n\n## 输入格式\n\n...\n\n## 输出格式\n\n...',
  standardCode: '#include <iostream>\n#include <vector>\n#include <algorithm>\n\nusing namespace std;\n\nint main() {\n    // 请在此处编写标准解法\n    ios::sync_with_stdio(false);\n    cin.tie(nullptr);\n    \n    return 0;\n}',
});

// 监听表单变化并保存到本地
watch(form, (newVal) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal));
}, { deep: true });

onMounted(async () => {
  // 尝试从本地加载
  const savedForm = localStorage.getItem(STORAGE_KEY);
  if (savedForm) {
    try {
      const parsed = JSON.parse(savedForm);
      form.value = { ...form.value, ...parsed };
    } catch (e) {
      console.error('Failed to parse saved form', e);
    }
  }

  try {
    const res = await getStrategies();
    strategies.value = res.data;
    // 如果没有本地保存的策略，则默认选中第一项
    if (strategies.value.length > 0 && form.value.strategyIds.length === 0) {
      form.value.strategyIds = [strategies.value[0].id];
    }
  } catch (error) {
    Message.error('获取策略列表失败');
  }
});

const handleReset = () => {
  localStorage.removeItem(STORAGE_KEY);
  form.value = {
    title: '',
    timeLimit: 1000,
    memoryLimit: 256,
    targetCases: 10,
    strategyIds: strategies.value.length > 0 ? [strategies.value[0].id] : [],
    description: '# 题目描述\n\n## 描述\n\n请在这里输入题目的 Markdown 描述...\n\n## 输入格式\n\n...\n\n## 输出格式\n\n...',
    standardCode: '#include <iostream>\n#include <vector>\n#include <algorithm>\n\nusing namespace std;\n\nint main() {\n    // 请在此处编写标准解法\n    ios::sync_with_stdio(false);\n    cin.tie(nullptr);\n    \n    return 0;\n}',
  };
  currentStep.value = 1;
  Message.info('配置已重置');
};

const nextStep = () => {
  if (currentStep.value === 1) {
    if (!form.value.title) {
      Message.warning('请输入题目名称');
      return;
    }
    if (form.value.strategyIds.length === 0) {
      Message.warning('请至少选择一种测试策略');
      return;
    }
  }
  currentStep.value++;
};

const handleSubmit = async () => {
  submitting.value = true;
  try {
    const res = await createTask({
      title: form.value.title,
      problemDescription: form.value.description,
      standardCode: form.value.standardCode,
      timeLimit: form.value.timeLimit,
      memoryLimit: form.value.memoryLimit,
      strategyIds: form.value.strategyIds,
      testcaseCount: form.value.targetCases,
    });
    Message.success({
      content: res.msg || '任务提交成功！固定扣除 5 MAI。',
      duration: 3000
    });
    // 提交成功后清除本地存储
    localStorage.removeItem(STORAGE_KEY);
    router.push({ name: 'Dashboard' }); 
  } catch (error) {
    console.error(error);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.task-create-wizard {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 60px;
}

.custom-steps {
  max-width: 800px;
  margin: 0 auto;
}

.editor-wrapper {
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
}

.step-container {
  width: 100%;
}

.strategy-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  width: 100%;
}

@media (max-width: 1200px) {
  .strategy-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.strategy-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.strategy-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: var(--mg-primary);
  transform: translateY(-2px);
}

.strategy-card.active {
  background: rgba(var(--arcoblue-6-rgb), 0.1);
  border-color: var(--mg-primary);
  box-shadow: 0 8px 20px rgba(var(--mg-primary-rgb), 0.15);
}

.strategy-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  color: var(--mg-text-2);
  font-size: 24px;
  transition: all 0.3s ease;
}

.active .strategy-icon {
  background: var(--mg-primary);
  color: white;
}

.strategy-info {
  flex: 1;
  min-width: 0;
}

.strategy-name {
  font-weight: 700;
  font-size: 15px;
  color: var(--mg-text-1);
  margin-bottom: 4px;
}

.strategy-desc {
  font-size: 12px;
  color: var(--mg-text-3);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.strategy-checkbox {
  font-size: 20px;
  color: var(--mg-primary);
}

.checkbox-placeholder {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

:deep(.arco-steps-item-title) {
  font-weight: 700;
  color: var(--mg-text-1) !important;
}

:deep(.arco-steps-item-description) {
  color: var(--mg-text-3) !important;
}

:deep(.arco-steps-icon) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  color: var(--mg-text-3) !important;
}

:deep(.arco-steps-item-active .arco-steps-icon) {
  background-color: var(--mg-primary) !important;
  color: #fff !important;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
