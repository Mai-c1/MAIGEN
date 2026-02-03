<template>
  <div class="login-container flex items-center justify-center min-h-screen relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-primary/20 rounded-full blur-[100px] animate-pulse"></div>
    <div class="absolute bottom-1/4 right-1/4 w-80 h-80 bg-purple-500/20 rounded-full blur-[100px] animate-pulse" style="animation-delay: 1s;"></div>

    <div class="glass-card p-12 rounded-[32px] w-full max-w-[480px] relative z-10 border border-white/10 shadow-2xl backdrop-blur-2xl">
      <div class="text-center mb-12">
        <h1 class="text-5xl font-black mg-text-gradient mb-3 tracking-tight drop-shadow-lg">
          {{ isLogin ? 'MAIGEN' : '加入我们' }}
        </h1>
        <p class="text-[var(--mg-text-2)] text-base font-medium opacity-80">
          {{ isLogin ? '开启您的 AI 创作之旅' : '创建一个账号以开始生成' }}
        </p>
      </div>
      
      <!-- 登录表单 -->
      <a-form v-if="isLogin" :model="loginForm" layout="vertical" @submit="handleLogin" class="custom-form">
        <a-form-item field="username" hide-label>
          <a-input v-model="loginForm.username" placeholder="请输入用户名或邮箱" size="large" class="glass-input">
            <template #prefix>
              <icon-user class="text-white/50" />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item field="password" hide-label>
          <a-input-password v-model="loginForm.password" placeholder="请输入密码" size="large" class="glass-input">
            <template #prefix>
              <icon-lock class="text-white/50" />
            </template>
          </a-input-password>
        </a-form-item>
        
        <div class="flex justify-between items-center mb-6 text-sm">
          <a-checkbox class="text-white/60">记住我</a-checkbox>
          <a href="javascript:;" class="text-primary hover:text-primary-light transition-colors">忘记密码?</a>
        </div>

        <a-button type="primary" long :loading="loading" html-type="submit" size="large" class="glow-btn h-12 rounded-xl text-lg font-bold">
          立即登录
        </a-button>
      </a-form>

      <!-- 注册表单 -->
      <a-form v-else :model="registerForm" layout="vertical" @submit="handleRegister" class="custom-form">
        <a-form-item field="username" hide-label>
          <a-input v-model="registerForm.username" placeholder="设置你的用户名" size="large" class="glass-input">
            <template #prefix>
              <icon-user class="text-white/50" />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item field="email" hide-label>
          <a-input v-model="registerForm.email" placeholder="请输入常用邮箱" size="large" class="glass-input">
            <template #prefix>
              <icon-email class="text-white/50" />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item field="code" hide-label>
          <div class="flex gap-3 w-full">
            <a-input v-model="registerForm.code" placeholder="6位验证码" size="large" class="glass-input">
              <template #prefix>
                <icon-safe class="text-white/50" />
              </template>
            </a-input>
            <a-button 
              :disabled="countdown > 0" 
              @click="handleSendCode" 
              size="large" 
              class="flex-shrink-0 rounded-xl bg-white/5 border-white/10 hover:bg-white/10 text-white/80"
              style="width: 130px"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </a-button>
          </div>
        </a-form-item>
        <a-form-item field="password" hide-label>
          <a-input-password v-model="registerForm.password" placeholder="设置密码 (至少6位)" size="large" class="glass-input">
            <template #prefix>
              <icon-lock class="text-white/50" />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item field="invitationCode" hide-label>
          <a-input v-model="registerForm.invitationCode" placeholder="邀请码 (选填)" size="large" class="glass-input">
            <template #prefix>
              <icon-gift class="text-white/50" />
            </template>
          </a-input>
        </a-form-item>
        <a-button type="primary" long :loading="loading" html-type="submit" size="large" class="glow-btn h-12 rounded-xl text-lg font-bold mt-2">
          立即注册
        </a-button>
      </a-form>

      <div class="mt-8 pt-6 border-t border-white/10 text-center text-[var(--mg-text-3)] text-sm">
        {{ isLogin ? '还没有账号?' : '已经有账号了?' }}
        <a href="javascript:;" class="text-primary hover:text-primary-light font-bold ml-1 transition-colors hover:underline decoration-2 underline-offset-4" @click="toggleMode">
          {{ isLogin ? '立即注册' : '返回登录' }}
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  background: #0a0a0a;
  background-image: 
    radial-gradient(circle at 15% 50%, rgba(var(--mg-primary-rgb), 0.08), transparent 25%),
    radial-gradient(circle at 85% 30%, rgba(168, 85, 247, 0.08), transparent 25%);
}

.mg-text-gradient {
  background: linear-gradient(135deg, #fff 0%, var(--mg-primary) 50%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 2px 10px rgba(var(--mg-primary-rgb), 0.3));
}

.glass-card {
  background: rgba(20, 20, 20, 0.6);
  backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 20px 40px -10px rgba(0,0,0,0.5);
}

:deep(.glass-input .arco-input-wrapper), 
:deep(.glass-input.arco-input-password) {
  background-color: rgba(0, 0, 0, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 12px;
  height: 50px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.glass-input .arco-input-wrapper:hover), 
:deep(.glass-input.arco-input-password:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  border-color: rgba(var(--mg-primary-rgb), 0.3) !important;
}

:deep(.glass-input .arco-input-wrapper.arco-input-focus), 
:deep(.glass-input.arco-input-password.arco-input-focus) {
  background-color: rgba(0, 0, 0, 0.4) !important;
  border-color: var(--mg-primary) !important;
  box-shadow: 0 0 0 4px rgba(var(--mg-primary-rgb), 0.15);
  transform: translateY(-1px);
}

:deep(.arco-input-prefix) {
  padding-right: 12px;
}

.glow-btn {
  box-shadow: 0 8px 20px -6px rgba(var(--mg-primary-rgb), 0.5);
  transition: all 0.3s ease;
}

.glow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px -8px rgba(var(--mg-primary-rgb), 0.6);
}

.glow-btn:active {
  transform: translateY(0);
}
</style>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { login, register, sendCode } from '@/api/auth';
import { useUserStore } from '@/store/user';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const isLogin = ref(true);
const countdown = ref(0);
let timer: any = null;

const loginForm = ref({
  username: '',
  password: '',
});

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  code: '',
  invitationCode: '',
});

const toggleMode = () => {
  isLogin.value = !isLogin.value;
};

const handleSendCode = async () => {
  if (!registerForm.value.email) {
    Message.warning('请先输入邮箱');
    return;
  }
  try {
    const res = await sendCode({ email: registerForm.value.email, type: 'register' });
    Message.success(res.msg || '验证码已发送，请注意查收');
    startCountdown();
  } catch (error) {
    console.error(error);
  }
};

const startCountdown = () => {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    Message.error('请输入用户名和密码');
    return;
  }
  
  loading.value = true;
  try {
    const res: any = await login(loginForm.value);
    userStore.setToken(res.data.tokenValue);
    userStore.setUserInfo({
      userId: res.data.userId,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
    });
    Message.success(res.msg || '登录成功');
    router.push('/dashboard');
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleRegister = async () => {
  const { username, email, password, code } = registerForm.value;
  if (!username || !email || !password || !code) {
    Message.warning('请填写完整注册信息');
    return;
  }
  
  loading.value = true;
  try {
    const res = await register(registerForm.value);
    Message.success(res.msg || '注册成功！请登录');
    isLogin.value = true;
    loginForm.value.username = email;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>
