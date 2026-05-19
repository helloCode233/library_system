<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">借阅管理</span>
      </div>
      <div class="page-card-body">
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户" width="100" />
          <el-table-column prop="bookTitle" label="书名" />
          <el-table-column prop="bookIsbn" label="ISBN" width="150" />
          <el-table-column prop="borrowDate" label="借书日期" width="160" />
          <el-table-column prop="dueDate" label="应还日期" width="160" />
          <el-table-column prop="returnDate" label="实际还书日期" width="160" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="warning" size="small">借阅中</el-tag>
              <el-tag v-else-if="row.status === 1" type="success" size="small">已归还</el-tag>
              <el-tag v-else type="danger" size="small">已逾期</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllBorrows } from '@/api/borrow'

const tableData = ref([])
const loadData = async () => { const res = await getAllBorrows(); if (res.code === 200) { tableData.value = res.data || [] } }
onMounted(() => { loadData() })
</script>
