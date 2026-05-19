<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">菜单管理</span>
        <el-button type="primary" @click="handleCreate(null)">新增菜单</el-button>
      </div>
      <div class="page-card-body">
        <el-table :data="tableData" stripe row-key="id">
          <el-table-column prop="menuName" label="菜单名称">
            <template #default="{ row }">
              <span :style="{ paddingLeft: (row._level * 20) + 'px' }">{{ row.menuName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="path" label="路径" />
          <el-table-column prop="component" label="组件" />
          <el-table-column prop="perms" label="权限标识" />
          <el-table-column prop="sort" label="排序" width="60" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleCreate(row)">添加子菜单</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="70px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如: system/UserManage" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="如: user:list" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)
const form = reactive({ id: null, menuName: '', parentId: 0, path: '', component: '', perms: '', sort: 0, icon: '', menuType: 1 })

const flattenTree = (nodes) => {
  const result = []
  const flatten = (list, level = 0) => {
    for (const node of list) { result.push({ ...node, _level: level }); if (node.children?.length) { flatten(node.children, level + 1) } }
  }
  flatten(nodes); return result
}
const loadData = async () => {
  const res = await getMenuTree()
  if (res.code === 200) { tableData.value = flattenTree(res.data || []) }
}
const handleCreate = (parent) => {
  Object.assign(form, { id: null, menuName: '', parentId: parent?.id || 0, path: '', component: '', perms: '', sort: 0, icon: '', menuType: 1 })
  dialogTitle.value = parent ? `新增子菜单（${parent.menuName}）` : '新增菜单'; dialogVisible.value = true
}
const handleEdit = (row) => {
  Object.assign(form, { id: row.id, menuName: row.menuName, parentId: row.parentId, path: row.path, component: row.component, perms: row.perms, sort: row.sort, icon: row.icon, menuType: row.menuType })
  dialogTitle.value = '编辑菜单'; dialogVisible.value = true
}
const handleSave = async () => {
  if (form.id) { await updateMenu(form) } else { await createMenu(form) }
  dialogVisible.value = false; ElMessage.success('保存成功'); loadData()
}
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该菜单？', '提示', { type: 'warning' })
  await deleteMenu(row.id); ElMessage.success('删除成功'); loadData()
}
onMounted(() => { loadData() })
</script>
