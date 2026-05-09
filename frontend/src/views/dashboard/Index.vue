<template>
  <div class="dashboard">
    <h2>欢迎使用图书馆管理系统</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>图书总数</span>
          </template>
          <div class="stat-value">1,234</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>借阅中</span>
          </template>
          <div class="stat-value">56</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>用户总数</span>
          </template>
          <div class="stat-value">89</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>
            <span>系统用户</span>
          </template>
          <div class="stat-value">{{ userStore.userInfo?.username || '-' }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
  color: #333;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  padding: 10px 0;
  color: #409eff;
}
</style>
