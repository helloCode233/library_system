<template>
  <div class="page-container" v-if="book">
    <div class="page-header">
      <div class="page-header-left">
        <el-button link @click="$router.back()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:18px;height:18px;vertical-align:-4px;margin-right:4px"><polyline points="15 18 9 12 15 6"/></svg>
          返回
        </el-button>
        <span class="page-title" style="margin-left:8px">{{ book.title }}</span>
      </div>
      <el-button type="primary" @click="handleBorrow">借阅此书</el-button>
    </div>
    <div class="page-card">
      <div class="page-card-body">
        <el-descriptions :column="2" border size="large">
          <el-descriptions-item label="书名">{{ book.title }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
          <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ book.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ book.stock }} / {{ book.total }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="book.status === 1 ? 'success' : 'danger'" size="small">
              {{ book.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ book.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- AI 相似图书推荐 -->
    <div v-if="similarBooks.length > 0" style="margin-top:24px">
      <el-divider />
      <h3 style="margin-bottom:16px;font-size:16px;font-weight:600">
        <el-icon style="margin-right:6px;vertical-align:middle"><Star /></el-icon>
        猜你喜欢（相似图书）
      </h3>
      <el-row :gutter="16">
        <el-col v-for="book in similarBooks" :key="book.id" :span="8" style="margin-bottom:16px">
          <el-card shadow="hover" @click="$router.push(`/book/${book.id}`)" style="cursor:pointer">
            <div style="font-size:14px;font-weight:600;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
              {{ book.title }}
            </div>
            <div style="font-size:12px;color:var(--color-text-muted);margin-bottom:4px">{{ book.author || '未知作者' }}</div>
            <el-tag size="small" type="success">相似 {{ (book.score * 100).toFixed(0) }}%</el-tag>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBook } from '@/api/book'
import { borrowBook } from '@/api/borrow'
import { getSimilarBooks } from '@/api/recommend'
import { Star } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const similarBooks = ref([])

const loadData = async () => { const res = await getBook(route.params.id); if (res.code === 200) { book.value = res.data } }
const loadSimilarBooks = async () => {
  try {
    const res = await getSimilarBooks(route.params.id, 6)
    if (res.code === 200) {
      similarBooks.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load similar books:', e)
  }
}
const handleBorrow = async () => {
  try { await borrowBook({ bookId: book.value.id }); ElMessage.success('借书成功'); loadData() } catch (e) { ElMessage.error(e.message || '借书失败') }
}
onMounted(() => { loadData(); loadSimilarBooks() })
</script>
