<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="app-sidebar">
      <div class="sidebar-brand" @click="$router.push('/dashboard')">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </div>
        <span class="brand-text">AI智能图书管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/books">
          <el-icon><Reading /></el-icon>
          <span>浏览图书</span>
        </el-menu-item>
        <el-sub-menu index="/system" v-if="isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/ai">
          <template #title>
            <el-icon><Cpu /></el-icon>
            <span>AI智能</span>
          </template>
          <el-menu-item index="/ai/search">
            <el-icon><Search /></el-icon>
            <span>智能搜索</span>
          </el-menu-item>
          <el-menu-item index="/ai/recommend">
            <el-icon><Star /></el-icon>
            <span>图书推荐</span>
          </el-menu-item>
          <el-menu-item index="/ai/hotbooks">
            <el-icon><TrendCharts /></el-icon>
            <span>借阅洞察</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/library">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>图书馆业务</span>
          </template>
          <el-menu-item index="/category" v-if="isAdmin">分类管理</el-menu-item>
          <el-menu-item index="/book">图书管理</el-menu-item>
          <el-menu-item index="/borrow/my">我的借阅</el-menu-item>
          <el-menu-item index="/borrow/manage" v-if="isAdmin">借阅管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
      <div class="sidebar-footer">
        <span class="version-tag">v1.0.0</span>
      </div>
    </aside>

    <!-- Main Area -->
    <div class="app-main">
      <header class="app-header">
        <div class="header-left">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;color:var(--color-text-muted)">
            <circle cx="12" cy="8" r="4"/><path d="M20 21a8 8 0 0 0-16 0"/>
          </svg>
          <span class="header-username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '未登录' }}</span>
        </div>
        <div class="header-right">
          <span class="header-role" v-if="userStore.userInfo?.roleName">{{ userStore.userInfo.roleName }}</span>
          <button class="logout-btn" @click="handleLogout" title="退出登录">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <span>退出</span>
          </button>
        </div>
      </header>
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, Setting, Reading, Cpu, Star, Search, TrendCharts } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const isAdmin = computed(() => userStore.userInfo?.roleName === '管理员')

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.app-sidebar {
  width: var(--sidebar-width);
  background: linear-gradient(180deg, #4C1D95 0%, #5B21B6 40%, #7C3AED 100%);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
}
.sidebar-brand {
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.brand-icon {
  width: 32px; height: 32px;
  background: rgba(255,255,255,0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-icon svg {
  width: 18px; height: 18px;
  color: #fff;
}
.brand-text {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: #fff;
  letter-spacing: 1px;
  white-space: nowrap;
}

/* Menu */
.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  padding: 8px 0;
}
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  margin: 2px 8px;
  border-radius: 8px;
  height: 42px;
  line-height: 42px;
  font-size: var(--font-size-base);
  transition: all 0.2s ease;
}
.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255,255,255,0.1) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255,255,255,0.18) !important;
  font-weight: 500;
}
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0,0,0,0.12) !important;
}
.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  height: 38px;
  line-height: 38px;
  font-size: var(--font-size-sm);
}

.sidebar-footer {
  padding: 12px 18px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.version-tag {
  font-size: 11px;
  color: rgba(255,255,255,0.35);
  letter-spacing: 1px;
}

/* Header */
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.app-header {
  height: var(--navbar-height);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-username {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-role {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: var(--color-primary-bg);
  padding: 2px 10px;
  border-radius: 100px;
  font-weight: 500;
}
.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  font-family: inherit;
  transition: all 0.2s ease;
}
.logout-btn svg {
  width: 16px; height: 16px;
}
.logout-btn:hover {
  background: #FEE2E2;
  color: var(--color-danger);
}

/* Content */
.app-content {
  flex: 1;
  overflow-y: auto;
  background: var(--color-bg);
}
</style>
