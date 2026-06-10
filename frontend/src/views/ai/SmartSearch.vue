<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">AI 智能搜索</span>
        <span style="font-size:13px;color:var(--color-text-muted);margin-left:12px">
          支持自然语言搜索，基于 TF-IDF 语义匹配
        </span>
      </div>
      <div class="page-card-body">
        <div class="search-bar" style="margin-bottom:20px;display:flex;gap:12px;align-items:center">
          <el-input
            v-model="query"
            placeholder="输入关键词描述，例如：科幻小说、中国古典名著、历史类书籍..."
            style="flex:1;max-width:560px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" :loading="loading" @click="handleSearch">
            <el-icon style="margin-right:4px"><Search /></el-icon>
            搜索
          </el-button>
          <el-button v-if="isAdmin" @click="handleRebuild" :loading="rebuilding" size="small" style="margin-left:4px">
            重建索引
          </el-button>
        </div>

        <el-table v-if="searched" :data="results" stripe empty-text="未找到匹配的图书，请尝试其他关键词">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="title" label="书名" min-width="160">
            <template #default="{ row }">
              <el-link type="primary" @click="$router.push(`/book/${row.id}`)">{{ row.title }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="isbn" label="ISBN" width="150" />
          <el-table-column prop="stock" label="库存" width="60" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="相关度" width="90">
            <template #default="{ row }">
              <span :style="{ color: row.score > 0.1 ? '#22c55e' : '#94a3b8' }">
                {{ (row.score * 100).toFixed(1) }}%
              </span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!searched" description="输入关键词开始 AI 智能搜索" :image-size="120" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiSearch, rebuildIndex } from '@/api/search'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const query = ref('')
const results = ref([])
const loading = ref(false)
const rebuilding = ref(false)
const searched = ref(false)

const isAdmin = ref(userStore.userInfo?.roleId === 1 || userStore.userInfo?.roleName === '管理员')

const handleSearch = async () => {
  if (!query.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  loading.value = true
  try {
    const res = await aiSearch({ query: query.value.trim(), limit: 20 })
    if (res.code === 200) {
      results.value = res.data || []
    }
    searched.value = true
  } catch (e) {
    ElMessage.error(e.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

const handleRebuild = async () => {
  rebuilding.value = true
  try {
    const res = await rebuildIndex()
    if (res.code === 200) {
      ElMessage.success(`索引重建完成，共 ${res.data?.indexedCount || 0} 本图书`)
    }
  } catch (e) {
    ElMessage.error('索引重建失败')
  } finally {
    rebuilding.value = false
  }
}
</script>
