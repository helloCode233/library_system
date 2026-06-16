<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="public-header-left" @click="$router.push('/books')" style="cursor:pointer">
        <div class="public-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
        </div>
        <span class="public-brand">AI智能图书管理</span>
      </div>
      <div class="public-header-right">
        <template v-if="token">
          <span class="header-user">{{ userInfo?.nickname || userInfo?.username }}</span>
          <el-button @click="$router.push('/dashboard')">进入系统</el-button>
          <el-button @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </header>
    <main class="public-main">
      <div class="detail-container" v-if="book">
        <el-button text @click="$router.push('/books')" style="margin-bottom:16px">
          &larr; 返回图书列表
        </el-button>
        <div class="detail-card">
          <div class="detail-header">
            <h2>{{ book.title }}</h2>
            <div class="detail-actions">
              <el-tag :type="book.status === 1 ? 'success' : 'danger'" size="large">
                {{ book.status === 1 ? '上架' : '下架' }}
              </el-tag>
              <el-button
                v-if="token"
                type="primary"
                :disabled="book.stock <= 0"
                @click="handleBorrow"
              >
                {{ book.stock > 0 ? '借阅此书' : '已借完' }}
              </el-button>
              <el-button v-else type="primary" @click="$router.push('/login')">
                登录后借阅
              </el-button>
            </div>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="书名">{{ book.title }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
            <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ book.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="库存">{{ book.stock }} / {{ book.total }}</el-descriptions-item>
            <el-descriptions-item label="入库时间">{{ book.createTime }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ book.description || '暂无简介' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <div v-else class="detail-loading">
        <el-icon class="is-loading" :size="32"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></el-icon>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBook } from '@/api/book'
import { borrowBook } from '@/api/borrow'

const route = useRoute()
const router = useRouter()
const token = localStorage.getItem('token')
const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
const book = ref(null)

const loadBook = async () => {
  try {
    const res = await getBook(route.params.id)
    if (res.code === 200) { book.value = res.data }
  } catch { /* silent */ }
}

const handleBorrow = async () => {
  try {
    await borrowBook({ bookId: book.value.id })
    ElMessage.success('借书成功')
    loadBook()
  } catch (e) {
    ElMessage.error(e.message || '借书失败')
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出')
  location.reload()
}

onMounted(loadBook)
</script>

<style scoped>
.public-layout { min-height: 100vh; background: var(--color-bg); display: flex; flex-direction: column; }
.public-header {
  height: 56px; background: #fff; border-bottom: 1px solid var(--color-border-light);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 40px; flex-shrink: 0;
}
.public-header-left { display: flex; align-items: center; gap: 10px; }
.public-logo {
  width: 32px; height: 32px; background: linear-gradient(135deg, #7C3AED, #5B21B6);
  border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff;
}
.public-logo svg { width: 18px; height: 18px; }
.public-brand { font-size: 16px; font-weight: 600; color: var(--color-primary-dark); letter-spacing: 1px; }
.public-header-right { display: flex; align-items: center; gap: 12px; }
.header-user { font-size: 14px; color: var(--color-text); font-weight: 500; }
.public-main { flex: 1; padding: 24px 40px; }
.detail-container { max-width: 900px; margin: 0 auto; }
.detail-card { background: #fff; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); padding: 24px; border: 1px solid var(--color-border-light); }
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.detail-header h2 { font-size: 24px; color: var(--color-text); margin: 0; }
.detail-actions { display: flex; gap: 12px; align-items: center; }
.detail-loading { display: flex; justify-content: center; padding: 80px 0; color: var(--color-text-muted); }
</style>
