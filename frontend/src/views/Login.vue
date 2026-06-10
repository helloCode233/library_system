<template>
  <div class="login-page">
    <div class="login-bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-card-wrapper">
      <div class="login-brand">
        <div class="login-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </div>
        <h1 class="login-title">AI智能图书管理系统</h1>
        <p class="login-subtitle">Library Management System</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            :prefix-icon="UserIcon"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="LockIcon"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <p class="login-footer">默认账户：admin / admin123</p>
  </div>
</template>

<script setup>
import { ref, reactive, h } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const svgIcon = (d) => ({
  render() {
    return h('svg', {
      viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor',
      'stroke-width': '1.5', 'stroke-linecap': 'round', 'stroke-linejoin': 'round',
      innerHTML: d, style: { width: '18px', height: '18px', display: 'block' }
    })
  }
})
const UserIcon = svgIcon('<circle cx="12" cy="8" r="4"/><path d="M20 21a8 8 0 0 0-16 0"/>')
const LockIcon = svgIcon('<rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>')

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(form)
    if (res.code === 200) {
      userStore.setAuth(res.data.token, res.data.user)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch {
    ElMessage.error('登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #7C3AED 0%, #5B21B6 40%, #4C1D95 100%);
  position: relative;
  overflow: hidden;
}
.login-bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.05);
}
.shape-1 {
  width: 600px; height: 600px;
  top: -200px; right: -150px;
}
.shape-2 {
  width: 400px; height: 400px;
  bottom: -100px; left: -80px;
}
.shape-3 {
  width: 200px; height: 200px;
  top: 40%; left: 55%;
}
.login-card-wrapper {
  position: relative;
  width: 400px;
  background: rgba(255,255,255,0.97);
  border-radius: var(--radius-lg);
  box-shadow: 0 20px 60px rgba(0,0,0,0.15), 0 0 0 1px rgba(255,255,255,0.1);
  padding: 48px 40px 40px;
  backdrop-filter: blur(10px);
}
.login-brand {
  text-align: center;
  margin-bottom: 36px;
}
.login-logo {
  width: 56px; height: 56px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #7C3AED, #5B21B6);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.login-logo svg {
  width: 28px; height: 28px;
}
.login-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary-dark);
  letter-spacing: 1px;
}
.login-subtitle {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 6px;
  font-weight: 300;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.login-form {
  margin-top: 8px;
}
.login-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px var(--color-border) inset;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-light) inset;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-primary) inset;
}
.login-btn {
  width: 100%;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  height: 44px;
  --el-button-bg-color: #7C3AED;
  --el-button-border-color: #7C3AED;
  --el-button-hover-bg-color: #6D28D9;
  --el-button-hover-border-color: #6D28D9;
}
.login-footer {
  position: relative;
  margin-top: 28px;
  color: rgba(255,255,255,0.6);
  font-size: var(--font-size-xs);
}
</style>
