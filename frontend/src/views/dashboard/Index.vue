<template>
  <div class="dashboard">
    <!-- Welcome -->
    <div class="welcome-card">
      <div class="welcome-content">
        <h2 class="welcome-title">
          欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}
        </h2>
        <p class="welcome-desc">图书馆管理系统 · 数据概览</p>
      </div>
      <div class="welcome-date">{{ today }}</div>
    </div>

    <!-- Stat Cards -->
    <div class="stat-grid">
      <div class="stat-card stat-books">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">1,234</span>
          <span class="stat-label">图书总数</span>
        </div>
        <div class="stat-trend up">+12 本月新增</div>
      </div>
      <div class="stat-card stat-borrow">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">56</span>
          <span class="stat-label">借阅中</span>
        </div>
        <div class="stat-trend up">+8 本月</div>
      </div>
      <div class="stat-card stat-users">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">89</span>
          <span class="stat-label">注册用户</span>
        </div>
        <div class="stat-trend up">+3 本月</div>
      </div>
      <div class="stat-card stat-available">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">1,178</span>
          <span class="stat-label">可借阅</span>
        </div>
        <div class="stat-trend">95.5% 在库率</div>
      </div>
    </div>

    <!-- Quick Actions & Recent -->
    <div class="dashboard-grid">
      <div class="page-card" style="flex:1">
        <div class="page-card-header">
          <span style="font-weight:600">快捷操作</span>
        </div>
        <div class="page-card-body">
          <div class="quick-actions">
            <button class="action-btn" @click="$router.push('/book')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <span>图书检索</span>
            </button>
            <button class="action-btn" @click="$router.push('/borrow/manage')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
              <span>借阅管理</span>
            </button>
            <button class="action-btn" @click="$router.push('/system/user')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/>
              </svg>
              <span>新增用户</span>
            </button>
            <button class="action-btn" @click="$router.push('/category')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
              </svg>
              <span>分类管理</span>
            </button>
          </div>
        </div>
      </div>
      <div class="page-card" style="flex:1">
        <div class="page-card-header">
          <span style="font-weight:600">系统信息</span>
        </div>
        <div class="page-card-body">
          <dl class="info-list">
            <div class="info-item">
              <dt>架构</dt>
              <dd>Spring Cloud + Nacos</dd>
            </div>
            <div class="info-item">
              <dt>网关</dt>
              <dd>Gateway :8080</dd>
            </div>
            <div class="info-item">
              <dt>认证</dt>
              <dd>JWT (HS384)</dd>
            </div>
            <div class="info-item">
              <dt>数据库</dt>
              <dd>MySQL 9.3</dd>
            </div>
            <div class="info-item">
              <dt>当前用户</dt>
              <dd>{{ userStore.userInfo?.username || '-' }}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${['日','一','二','三','四','五','六'][d.getDay()]}`
})
</script>

<style scoped>
.dashboard {
  padding: var(--spacing-lg);
  max-width: 1280px;
  margin: 0 auto;
}

/* Welcome */
.welcome-card {
  background: linear-gradient(135deg, #7C3AED, #5B21B6);
  border-radius: var(--radius-lg);
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}
.welcome-title {
  color: #fff;
  font-size: var(--font-size-xl);
  font-weight: 600;
}
.welcome-desc {
  color: rgba(255,255,255,0.7);
  font-size: var(--font-size-sm);
  margin-top: 4px;
}
.welcome-date {
  color: rgba(255,255,255,0.6);
  font-size: var(--font-size-sm);
}

/* Stat Grid */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}
.stat-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.stat-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-icon svg { width: 20px; height: 20px; }
.stat-books .stat-icon { background: #EDE9FE; color: #7C3AED; }
.stat-borrow .stat-icon { background: #FEF3C7; color: #D97706; }
.stat-users .stat-icon { background: #DBEAFE; color: #2563EB; }
.stat-available .stat-icon { background: #D1FAE5; color: #059669; }
.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.stat-trend {
  font-size: 12px;
  color: var(--color-text-muted);
}
.stat-trend.up { color: #059669; }

/* Grid */
.dashboard-grid {
  display: flex;
  gap: var(--spacing-md);
}

/* Quick Actions */
.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-sm);
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--color-bg);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}
.action-btn svg { width: 18px; height: 18px; flex-shrink: 0; }
.action-btn:hover {
  background: var(--color-primary-bg);
  border-color: var(--color-primary-light);
  color: var(--color-primary);
}

/* Info List */
.info-list {
  display: flex;
  flex-direction: column;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light);
}
.info-item:last-child {
  border-bottom: none;
}
.info-item dt {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.info-item dd {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: 500;
}

@media (max-width: 1024px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .dashboard-grid { flex-direction: column; }
}
@media (max-width: 640px) {
  .stat-grid { grid-template-columns: 1fr; }
}
</style>
