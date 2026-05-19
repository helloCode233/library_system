<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">用户管理</span>
        <el-button type="primary" @click="handleCreate">新增用户</el-button>
      </div>
      <div class="page-card-body">
        <div class="search-bar" style="margin-bottom:16px">
          <el-input
            v-model="searchUsername"
            placeholder="搜索用户名"
            style="width:220px"
            clearable
            @keyup.enter="loadData"
          />
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          style="margin-top:16px"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="70px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '留空则不修改' : ''" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="选择角色" style="width:100%">
            <el-option v-for="role in allRoles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
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
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { getAllRoles } from '@/api/role'

const searchUsername = ref('')
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const allRoles = ref([])

const form = reactive({ id: null, username: '', password: '', nickname: '', roleId: null })

const loadData = async () => {
  const res = await getUserList({ pageNum: pageNum.value, pageSize: pageSize.value, username: searchUsername.value })
  if (res.code === 200) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
}
const loadRoles = async () => {
  const res = await getAllRoles()
  if (res.code === 200) { allRoles.value = res.data || [] }
}
const handleCreate = () => {
  Object.assign(form, { id: null, username: '', password: '', nickname: '', roleId: null })
  dialogTitle.value = '新增用户'; dialogVisible.value = true
}
const handleEdit = (row) => {
  Object.assign(form, { id: row.id, username: row.username, password: '', nickname: row.nickname, roleId: row.roleId })
  dialogTitle.value = '编辑用户'; dialogVisible.value = true
}
const handleSave = async () => {
  if (form.id) { await updateUser(form) } else { await createUser(form) }
  dialogVisible.value = false; ElMessage.success('保存成功'); loadData()
}
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该用户？', '提示', { type: 'warning' })
  await deleteUser(row.id); ElMessage.success('删除成功'); loadData()
}
onMounted(() => { loadData(); loadRoles() })
</script>
