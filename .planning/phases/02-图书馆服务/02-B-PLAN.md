---
phase: "02-图书馆服务"
plan: "B"
type: "execute"
wave: 1
depends_on: []
files_modified:
  - "backend/src/main/java/com/library/entity/Book.java"
  - "backend/src/main/java/com/library/mapper/BookMapper.java"
  - "backend/src/main/java/com/library/service/impl/BookServiceImpl.java"
  - "backend/src/main/java/com/library/controller/BookController.java"
  - "frontend/src/api/book.js"
  - "frontend/src/views/book/Index.vue"
  - "frontend/src/views/book/Detail.vue"
autonomous: true
requirements:
  - "BOOK-01"
  - "BOOK-02"
  - "BOOK-03"
  - "BOOK-04"
  - "BOOK-05"
  - "BOOK-06"
must_haves:
  truths:
    - "管理员可以增删改查图书"
    - "用户可以搜索图书"
    - "用户可以查看图书详情"
  artifacts:
    - path: "backend/src/main/java/com/library/controller/BookController.java"
      provides: "图书 CRUD + 搜索 API"
      exports: "GET/POST/PUT/DELETE /api/book"
---

# Plan 02-B: 图书管理

## Context
@.planning/phases/02-图书馆服务/02-CONTEXT.md
@.planning/phases/02-图书馆服务/02-A-PLAN.md

## Tasks

### Task 1: 后端图书实体、Mapper、Service、Controller

**backend/src/main/java/com/library/entity/Book.java**:
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Long categoryId;
    private String description;
    private String coverUrl;
    private Integer stock;
    private Integer total;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // transient fields for display
    private String categoryName;
}
```

**backend/src/main/java/com/library/mapper/BookMapper.java**:
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
```

**backend/src/main/java/com/library/service/impl/BookServiceImpl.java**:
```java
package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl {

    @Autowired
    private BookMapper bookMapper;

    public Page<Book> list(Integer pageNum, Integer pageSize, String title, String author, Long categoryId) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Book::getTitle, title);
        }
        if (author != null && !author.isEmpty()) {
            wrapper.like(Book::getAuthor, author);
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(page, wrapper);
    }

    public Book getById(Long id) {
        return bookMapper.selectById(id);
    }

    public void create(Book book) {
        // 检查 ISBN 唯一
        Book exist = bookMapper.selectOne(
                new LambdaQueryWrapper<Book>().eq(Book::getIsbn, book.getIsbn())
        );
        if (exist != null) {
            throw new RuntimeException("ISBN 已存在");
        }
        if (book.getTotal() == null) {
            book.setTotal(book.getStock());
        }
        bookMapper.insert(book);
    }

    public void update(Book book) {
        bookMapper.updateById(book);
    }

    public void delete(Long id) {
        bookMapper.deleteById(id);
    }

    public void updateStock(Long id, int delta) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        int newStock = book.getStock() + delta;
        if (newStock < 0) {
            throw new RuntimeException("库存不足");
        }
        book.setStock(newStock);
        bookMapper.updateById(book);
    }
}
```

**backend/src/main/java/com/library/controller/BookController.java**:
```java
package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.impl.BookServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookServiceImpl bookService;

    @GetMapping("/list")
    public Result<Page<Book>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(bookService.list(pageNum, pageSize, title, author, categoryId));
    }

    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Book book) {
        bookService.create(book);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Book book) {
        bookService.update(book);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success("删除成功", null);
    }
}
```

### Task 2: 前端图书 API 和页面

**frontend/src/api/book.js**:
```javascript
import request from './request'

export function getBookList(params) {
  return request({ url: '/book/list', method: 'get', params })
}

export function getBook(id) {
  return request({ url: `/book/${id}`, method: 'get' })
}

export function createBook(data) {
  return request({ url: '/book', method: 'post', data })
}

export function updateBook(data) {
  return request({ url: '/book', method: 'put', data })
}

export function deleteBook(id) {
  return request({ url: `/book/${id}`, method: 'delete' })
}
```

**frontend/src/views/book/Index.vue**:
```vue
<template>
  <div class="book-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">新增图书</el-button>
      <el-input
        v-model="searchTitle"
        placeholder="搜索书名"
        style="width: 150px; margin-left: 10px"
      />
      <el-input
        v-model="searchAuthor"
        placeholder="搜索作者"
        style="width: 150px; margin-left: 10px"
      />
      <el-select v-model="searchCategoryId" placeholder="选择分类" clearable style="width: 150px; margin-left: 10px">
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 10px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="书名" />
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="isbn" label="ISBN" width="150" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
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
      style="margin-top: 10px"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" label-width="80">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
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
import { getBookList, createBook, updateBook, deleteBook } from '@/api/book'
import { getAllCategories } from '@/api/category'
import { borrowBook } from '@/api/borrow'

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

const form = reactive({
  id: null,
  title: '',
  author: '',
  isbn: '',
  categoryId: null,
  description: '',
  stock: 0,
  status: 1
})

const loadData = async () => {
  const res = await getBookList({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    title: searchTitle.value,
    author: searchAuthor.value,
    categoryId: searchCategoryId.value
  })
  if (res.code === 200) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const loadCategories = async () => {
  const res = await getAllCategories()
  if (res.code === 200) {
    categories.value = res.data || []
  }
}

const handleCreate = () => {
  Object.assign(form, { id: null, title: '', author: '', isbn: '', categoryId: null, description: '', stock: 0, status: 1 })
  dialogTitle.value = '新增图书'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id, title: row.title, author: row.author, isbn: row.isbn,
    categoryId: row.categoryId, description: row.description, stock: row.stock, status: row.status
  })
  dialogTitle.value = '编辑图书'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.id) {
    await updateBook(form)
  } else {
    await createBook(form)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除?', '提示')
  await deleteBook(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleBorrow = async (row) => {
  try {
    await borrowBook({ bookId: row.id })
    ElMessage.success('借书成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '借书失败')
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
```

**frontend/src/views/book/Detail.vue** (optional book detail page):
```vue
<template>
  <div class="book-detail" v-if="book">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ book.title }}</span>
          <el-button link type="primary" @click="$router.back()">返回</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ book.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ book.stock }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="book.status === 1 ? 'success' : 'danger'">
            {{ book.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ book.description }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 20px">
        <el-button type="primary" @click="handleBorrow">借阅此书</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBook } from '@/api/book'
import { borrowBook } from '@/api/borrow'

const route = useRoute()
const router = useRouter()
const book = ref(null)

const loadData = async () => {
  const res = await getBook(route.params.id)
  if (res.code === 200) {
    book.value = res.data
  }
}

const handleBorrow = async () => {
  try {
    await borrowBook({ bookId: book.value.id })
    ElMessage.success('借书成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '借书失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

## Verification
- Run `cd backend && mvn compile -q 2>&1 | head -20`
- Run `cd frontend && npm run build 2>&1 | tail -10`

## After Completion
Create `.planning/phases/02-图书馆服务/02-B-SUMMARY.md`
