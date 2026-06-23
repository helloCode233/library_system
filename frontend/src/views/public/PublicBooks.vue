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
      <div class="public-banner">
        <h2>图书浏览</h2>
        <p>探索馆藏图书，发现你的下一本读物</p>
      </div>
      <div class="public-content">
        <div class="search-bar" style="margin-bottom:20px">
          <el-input v-model="searchTitle" placeholder="书名" style="width:160px" clearable />
          <el-input v-model="searchAuthor" placeholder="作者" style="width:160px" clearable />
          <el-select v-model="searchCategoryId" placeholder="分类" clearable style="width:160px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="searchTitle='';searchAuthor='';searchCategoryId=null;loadData()">重置</el-button>
        </div>
        <el-table :data="tableData" stripe>
          <el-table-column label="书名">
            <template #default="{ row }">
              <router-link :to="'/books/' + row.id" class="book-link">{{ row.title }}</router-link>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="140" />
          <el-table-column prop="isbn" label="ISBN" width="160" />
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="stock" label="库存" width="70" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          style="margin-top:16px;justify-content:center;display:flex"
          @current-change="loadData"
        />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookList } from '@/api/book'
import { getAllCategories } from '@/api/category'

const router = useRouter()
const token = localStorage.getItem('token')
const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

const tableData = ref([])
const categories = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchTitle = ref('')
const searchAuthor = ref('')
const searchCategoryId = ref(null)

const loadData = async () => {
  try {
    const res = await getBookList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      title: searchTitle.value,
      author: searchAuthor.value,
      categoryId: searchCategoryId.value
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    // silent fail for public browsing
  }
}

const loadCategories = async () => {
  try {
    const res = await getAllCategories()
    if (res.code === 200) { categories.value = res.data || [] }
  } catch { /* silent */ }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出')
  location.reload()
}

onMounted(() => { loadData(); loadCategories() })
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
.public-main { flex: 1; }
.public-banner {
  background: linear-gradient(135deg, #7C3AED 0%, #5B21B6 40%, #4C1D95 100%);
  padding: 48px 40px 36px; color: #fff; text-align: center;
}
.public-banner h2 { font-size: 28px; font-weight: 700; margin: 0 0 8px; }
.public-banner p { font-size: 14px; opacity: 0.8; margin: 0; }
.public-content { max-width: 1100px; margin: -16px auto 40px; padding: 24px; background: #fff; border-radius: var(--radius-md); box-shadow: var(--shadow-md); }
.search-bar { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.book-link { color: var(--color-primary); text-decoration: none; font-weight: 500; }
.book-link:hover { text-decoration: underline; }
</style>
