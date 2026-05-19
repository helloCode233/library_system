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
const book = ref(null)

const loadData = async () => { const res = await getBook(route.params.id); if (res.code === 200) { book.value = res.data } }
const handleBorrow = async () => {
  try { await borrowBook({ bookId: book.value.id }); ElMessage.success('借书成功'); loadData() } catch (e) { ElMessage.error(e.message || '借书失败') }
}
onMounted(() => { loadData() })
</script>
