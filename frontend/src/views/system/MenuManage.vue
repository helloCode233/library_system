<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <span>菜单管理</span>
      </template>
      <el-table :data="[]" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="path" label="路由" />
        <el-table-column prop="component" label="组件" />
        <el-table-column prop="icon" label="图标" />
        <el-table-column prop="sort" label="排序" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.menu-manage {
  padding: 0;
}
</style>
