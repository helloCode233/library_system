<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">AI 图书推荐</span>
        <span style="font-size:13px;color:var(--color-text-muted);margin-left:12px">
          基于 TF-IDF 内容相似度和借阅历史智能推荐
        </span>
      </div>
      <div class="page-card-body">
        <!-- 个性化推荐区域 -->
        <h3 style="margin-bottom:16px;font-size:16px;font-weight:600">
          <el-icon style="margin-right:6px;vertical-align:middle"><Star /></el-icon>
          猜你喜欢
        </h3>
        <el-row v-if="personalRecs.length > 0" :gutter="16">
          <el-col v-for="book in personalRecs" :key="book.id" :span="6" style="margin-bottom:16px">
            <el-card shadow="hover" @click="$router.push(`/book/${book.id}`)" style="cursor:pointer">
              <div style="font-size:15px;font-weight:600;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
                {{ book.title }}
              </div>
              <div style="font-size:13px;color:var(--color-text-muted);margin-bottom:4px">{{ book.author || '未知作者' }}</div>
              <el-tag size="small" type="success">相似度 {{ (book.score * 100).toFixed(0) }}%</el-tag>
              <el-tag size="small" style="margin-left:6px" v-if="book.stock > 0" type="warning">库存 {{ book.stock }}</el-tag>
              <el-tag size="small" style="margin-left:6px" v-else type="danger">已借完</el-tag>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="借阅更多图书后，将为您生成个性化推荐" :image-size="80" />

        <!-- 分隔线 -->
        <el-divider style="margin:24px 0" />

        <!-- 热门图书排行榜 -->
        <h3 style="margin-bottom:16px;font-size:16px;font-weight:600">
          <el-icon style="margin-right:6px;vertical-align:middle"><TrendCharts /></el-icon>
          热门图书排行榜
        </h3>
        <el-table v-if="hotBooks.length > 0" :data="hotBooks" stripe>
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="title" label="书名" min-width="160">
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/book/${row.id}`)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="130" />
          <el-table-column prop="stock" label="库存" width="60" />
          <el-table-column label="被借次数" width="100">
            <template #default="{ row }">
              <el-tag type="warning" size="small">{{ row.borrowCount || 0 }} 次</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无借阅数据" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Star, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRecommendForUser, getHotBooks } from '@/api/recommend'

const personalRecs = ref([])
const hotBooks = ref([])

onMounted(async () => {
  try {
    const res = await getRecommendForUser(8)
    if (res.code === 200) {
      personalRecs.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load recommendations:', e)
  }

  try {
    const res = await getHotBooks(10)
    if (res.code === 200) {
      hotBooks.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load hot books:', e)
  }
})
</script>
