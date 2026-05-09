<template>
  <div class="role-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">新增角色</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 10px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="roleName" label="角色名" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="handleAssignMenu(row)">分配菜单</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 10px"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80">
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="400px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignMenus } from '@/api/role'
import { getMenuTree } from '@/api/menu'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const menuTree = ref([])
const menuTreeRef = ref(null)
const currentRoleId = ref(null)

const form = reactive({ id: null, roleName: '', description: '' })

const loadData = async () => {
  const res = await getRoleList({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (res.code === 200) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const loadMenuTree = async () => {
  const res = await getMenuTree()
  if (res.code === 200) {
    menuTree.value = res.data || []
  }
}

const handleCreate = () => {
  Object.assign(form, { id: null, roleName: '', description: '' })
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { id: row.id, roleName: row.roleName, description: row.description })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.id) {
    await updateRole(form)
  } else {
    await createRole(form)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除?', '提示')
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  menuDialogVisible.value = true
  await loadMenuTree()
  const res = await getRoleMenus(row.id)
  if (res.code === 200) {
    const checkedIds = res.data.map(m => m.id)
    nextTick(() => {
      menuTreeRef.value?.setCheckedKeys(checkedIds)
    })
  }
}

const handleSaveMenus = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  await assignMenus(currentRoleId.value, checkedKeys)
  menuDialogVisible.value = false
  ElMessage.success('分配成功')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>