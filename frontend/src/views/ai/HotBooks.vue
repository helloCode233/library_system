<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">AI 借阅洞察</span>
        <span style="font-size:13px;color:var(--color-text-muted);margin-left:12px">
          基于借阅数据的多维度统计分析
        </span>
      </div>
      <div class="page-card-body">
        <!-- 概览统计卡片 -->
        <el-row :gutter="16" style="margin-bottom:24px">
          <el-col :span="6" v-for="card in overviewCards" :key="card.label">
            <el-card shadow="hover">
              <div style="text-align:center">
                <div style="font-size:28px;font-weight:700;color:var(--color-primary)">{{ card.value }}</div>
                <div style="font-size:13px;color:var(--color-text-muted);margin-top:4px">{{ card.label }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 热门图书排行 -->
        <h3 style="margin-bottom:16px;font-size:16px;font-weight:600">
          <el-icon style="margin-right:6px;vertical-align:middle"><TrendCharts /></el-icon>
          热门图书排行 TOP 10
        </h3>
        <el-table :data="hotBooks" stripe style="margin-bottom:24px" empty-text="暂无借阅数据">
          <el-table-column prop="rank" label="排名" width="60" />
          <el-table-column prop="title" label="书名" min-width="160">
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/book/${row.bookId}`)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="130" />
          <el-table-column prop="stock" label="库存" width="70" />
          <el-table-column label="被借次数" width="110">
            <template #default="{ row }">
              <el-tag type="warning" size="small">{{ row.borrowCount }} 次</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-row :gutter="16">
          <!-- 月度借阅趋势 -->
          <el-col :span="12">
            <h3 style="margin-bottom:16px;font-size:16px;font-weight:600">
              <el-icon style="margin-right:6px;vertical-align:middle"><DataLine /></el-icon>
              月度借阅趋势
            </h3>
            <el-table :data="monthlyTrend" stripe max-height="360" empty-text="暂无数据">
              <el-table-column prop="month" label="月份" width="120" />
              <el-table-column prop="count" label="借阅数量">
                <template #default="{ row }">
                  <span style="font-weight:600">{{ row.count }}</span>
                  <span style="margin-left:6px;color:var(--color-text-muted);font-size:12px">册</span>
                </template>
              </el-table-column>
            </el-table>
          </el-col>

          <!-- 分类借阅分布 -->
          <el-col :span="12">
            <h3 style="margin-bottom:16px;font-size:16px;font-weight:600">
              <el-icon style="margin-right:6px;vertical-align:middle"><PieChart /></el-icon>
              分类借阅分布
            </h3>
            <el-table :data="categoryDist" stripe max-height="360" empty-text="暂无数据">
              <el-table-column prop="categoryName" label="分类" />
              <el-table-column prop="borrowCount" label="借阅次数" width="100" />
              <el-table-column label="占比" width="100">
                <template #default="{ row }">
                  <el-progress :percentage="row.percentage" :stroke-width="6" :show-text="true" />
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { TrendCharts, DataLine, PieChart } from '@element-plus/icons-vue'
import { getHotBooksRank, getMonthlyTrend, getCategoryDistribution, getOverview } from '@/api/insight'

const overviewCards = ref([
  { label: '总借阅次数', value: 0 },
  { label: '借阅中', value: 0 },
  { label: '已归还', value: 0 },
  { label: '涉及用户', value: 0 }
])
const hotBooks = ref([])
const monthlyTrend = ref([])
const categoryDist = ref([])

onMounted(async () => {
  try {
    const res = await getOverview()
    if (res.code === 200 && res.data) {
      overviewCards.value = [
        { label: '总借阅次数', value: res.data.totalBorrows || 0 },
        { label: '借阅中', value: res.data.activeBorrows || 0 },
        { label: '已归还', value: res.data.totalReturns || 0 },
        { label: '涉及用户', value: res.data.totalUsers || 0 }
      ]
    }
  } catch (e) {
    console.error('Failed to load overview:', e)
  }

  try {
    const res = await getHotBooksRank(10)
    if (res.code === 200) hotBooks.value = res.data || []
  } catch (e) {
    console.error('Failed to load hot books:', e)
  }

  try {
    const res = await getMonthlyTrend(12)
    if (res.code === 200) monthlyTrend.value = res.data || []
  } catch (e) {
    console.error('Failed to load trend:', e)
  }

  try {
    const res = await getCategoryDistribution()
    if (res.code === 200) categoryDist.value = res.data || []
  } catch (e) {
    console.error('Failed to load category distribution:', e)
  }
})
</script>
