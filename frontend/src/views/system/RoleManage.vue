<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <span>角色管理</span>
      </template>
      <el-table :data="[]" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.role-manage {
  padding: 0;
}
</style>
