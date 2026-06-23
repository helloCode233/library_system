<template>
  <div class="page-container">
    <div class="page-card">
      <div class="page-card-header">
        <span class="page-title">图书管理</span>
        <el-button type="primary" v-if="isAdmin" @click="handleCreate">新增图书</el-button>
      </div>
      <div class="page-card-body">
        <div class="search-bar" style="margin-bottom:16px">
          <el-input v-model="searchTitle" placeholder="书名" style="width:140px" clearable />
          <el-input v-model="searchAuthor" placeholder="作者" style="width:140px" clearable />
          <el-select v-model="searchCategoryId" placeholder="分类" clearable style="width:140px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="searchTitle='';searchAuthor='';searchCategoryId=null;loadData()">重置</el-button>
        </div>
        <el-table :data="tableData" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="书名" />
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
          <el-table-column v-if="isAdmin" label="操作" width="220">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="success" size="small" @click="handleBorrow(row)">借书</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="70px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookList, createBook, updateBook, deleteBook } from '@/api/book'
import { getAllCategories } from '@/api/category'
import { borrowBook } from '@/api/borrow'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.roleName === '管理员')

const tableData = ref([])
const categories = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增图书')
const formRef = ref(null)
const searchTitle = ref('')
const searchAuthor = ref('')
const searchCategoryId = ref(null)
const form = reactive({ id: null, title: '', author: '', isbn: '', categoryId: null, description: '', stock: 0, status: 1 })

const loadData = async () => {
  const res = await getBookList({ pageNum: pageNum.value, pageSize: pageSize.value, title: searchTitle.value, author: searchAuthor.value, categoryId: searchCategoryId.value })
  if (res.code === 200) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
}
const loadCategories = async () => {
  const res = await getAllCategories()
  if (res.code === 200) { categories.value = res.data || [] }
}
const handleCreate = () => {
  Object.assign(form, { id: null, title: '', author: '', isbn: '', categoryId: null, description: '', stock: 0, status: 1 })
  dialogTitle.value = '新增图书'; dialogVisible.value = true
}
const handleEdit = (row) => {
  Object.assign(form, { id: row.id, title: row.title, author: row.author, isbn: row.isbn, categoryId: row.categoryId, description: row.description, stock: row.stock, status: row.status })
  dialogTitle.value = '编辑图书'; dialogVisible.value = true
}
const handleSave = async () => {
  if (form.id) { await updateBook(form) } else { await createBook(form) }
  dialogVisible.value = false; ElMessage.success('保存成功'); loadData()
}
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该图书？', '提示', { type: 'warning' })
  await deleteBook(row.id); ElMessage.success('删除成功'); loadData()
}
const handleBorrow = async (row) => {
  try { await borrowBook({ bookId: row.id }); ElMessage.success('借书成功'); loadData() } catch (e) { ElMessage.error(e.message || '借书失败') }
}
onMounted(() => { loadData(); loadCategories() })
</script>
