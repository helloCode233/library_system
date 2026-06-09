---
phase: "02-图书馆服务"
plan: "A"
type: "execute"
wave: 1
depends_on: []
files_modified:
  - "sql/init.sql"
  - "backend/src/main/java/com/library/entity/Category.java"
  - "backend/src/main/java/com/library/mapper/CategoryMapper.java"
  - "backend/src/main/java/com/library/service/impl/CategoryServiceImpl.java"
  - "backend/src/main/java/com/library/controller/system/CategoryController.java"
  - "frontend/src/api/category.js"
  - "frontend/src/views/category/Index.vue"
autonomous: true
requirements:
  - "CATE-01"
  - "CATE-02"
  - "CATE-03"
  - "CATE-04"
  - "CATE-05"
must_haves:
  truths:
    - "数据库包含 sys_category 表"
    - "管理员可以增删改查分类"
    - "图书归属某个分类"
  artifacts:
    - path: "sql/init.sql"
      provides: "sys_category 表结构"
    - path: "backend/src/main/java/com/library/controller/system/CategoryController.java"
      provides: "分类 CRUD API"
      exports: "GET/POST/PUT/DELETE /api/category"
---

# Plan 02-A: 分类管理与数据库扩展

## Tasks

### Task 1: 更新数据库初始化脚本
File: sql/init.sql

在现有 init.sql 末尾追加（不重复创建已存在的表）：

```sql
USE library;

-- 图书分类表
CREATE TABLE IF NOT EXISTS sys_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
  description VARCHAR(200) COMMENT '描述',
  sort INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书表
CREATE TABLE IF NOT EXISTS sys_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL COMMENT '书名',
  author VARCHAR(50) COMMENT '作者',
  isbn VARCHAR(50) UNIQUE COMMENT 'ISBN',
  category_id BIGINT COMMENT '分类ID',
  description TEXT COMMENT '描述',
  cover_url VARCHAR(200) COMMENT '封面URL',
  stock INT DEFAULT 0 COMMENT '库存数量',
  total INT DEFAULT 0 COMMENT '总数量',
  status TINYINT DEFAULT 1 COMMENT '状态：0下架 1上架',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_title (title),
  INDEX idx_author (author),
  INDEX idx_category_id (category_id),
  INDEX idx_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS sys_borrow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  book_id BIGINT NOT NULL COMMENT '图书ID',
  borrow_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '借书日期',
  due_date DATETIME COMMENT '应还日期',
  return_date DATETIME COMMENT '实际还书日期',
  status TINYINT DEFAULT 0 COMMENT '状态：0借阅中 1已归还 2已逾期',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_book_id (book_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 初始分类数据
INSERT INTO sys_category (name, description, sort) VALUES
  ('文学', '文学作品类书籍', 1),
  ('科技', '科学技术类书籍', 2),
  ('历史', '历史类书籍', 3),
  ('儿童', '儿童读物', 4),
  ('教育', '教育类书籍', 5);

-- 初始图书数据
INSERT INTO sys_book (title, author, isbn, category_id, description, stock, total) VALUES
  ('红楼梦', '曹雪芹', '978-7-5322-0001-2', 1, '中国古典四大名著之一', 3, 5),
  ('西游记', '吴承恩', '978-7-5322-0002-9', 1, '中国古典四大名著之一', 2, 3),
  ('三体', '刘慈欣', '978-7-5366-0001-3', 2, '科幻小说', 5, 8),
  ('人类简史', '尤瓦尔·赫拉利', '978-7-5322-0003-6', 3, '人类历史综述', 4, 6),
  ('小王子', '安托万·德·圣-埃克苏佩里', '978-7-5322-0004-3', 4, '儿童文学经典', 6, 10);
```

### Task 2: 后端分类实体、Mapper、Service、Controller

**backend/src/main/java/com/library/entity/Category.java**:
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**backend/src/main/java/com/library/mapper/CategoryMapper.java**:
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
```

**backend/src/main/java/com/library/service/impl/CategoryServiceImpl.java**:
```java
package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Category;
import com.library.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl {

    @Autowired
    private CategoryMapper categoryMapper;

    public Page<Category> list(Integer pageNum, Integer pageSize) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getSort);
        return categoryMapper.selectPage(page, wrapper);
    }

    public java.util.List<Category> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }

    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    public void create(Category category) {
        Category exist = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName())
        );
        if (exist != null) {
            throw new RuntimeException("分类名称已存在");
        }
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
```

**backend/src/main/java/com/library/controller/system/CategoryController.java**:
```java
package com.library.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.Category;
import com.library.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    @GetMapping("/list")
    public Result<Page<Category>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(categoryService.list(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Category>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Category category) {
        categoryService.create(category);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}
```

### Task 3: 前端分类 API 和页面

**frontend/src/api/category.js**:
```javascript
import request from './request'

export function getCategoryList(params) {
  return request({ url: '/category/list', method: 'get', params })
}

export function getAllCategories() {
  return request({ url: '/category/all', method: 'get' })
}

export function getCategory(id) {
  return request({ url: `/category/${id}`, method: 'get' })
}

export function createCategory(data) {
  return request({ url: '/category', method: 'post', data })
}

export function updateCategory(data) {
  return request({ url: '/category', method: 'put', data })
}

export function deleteCategory(id) {
  return request({ url: `/category/${id}`, method: 'delete' })
}
```

**frontend/src/views/category/Index.vue**:
```vue
<template>
  <div class="category-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">新增分类</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 10px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
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
      style="margin-top: 10px"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  description: '',
  sort: 0,
  status: 1
})

const loadData = async () => {
  const res = await getCategoryList({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (res.code === 200) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const handleCreate = () => {
  Object.assign(form, { id: null, name: '', description: '', sort: 0, status: 1 })
  dialogTitle.value = '新增分类'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { id: row.id, name: row.name, description: row.description, sort: row.sort, status: row.status })
  dialogTitle.value = '编辑分类'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.id) {
    await updateCategory(form)
  } else {
    await createCategory(form)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除?', '提示')
  await deleteCategory(row.id)
  ElMessage.success('删除成功')
  loadData()
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
```

## Verification
- Run `mysql -uroot -proot -e "USE library; SHOW TABLES;"` — should see sys_category, sys_book, sys_borrow
- Run `cd backend && mvn compile -q 2>&1 | head -20`
- Run `cd frontend && npm run build 2>&1 | tail -10`

## After Completion
Create `.planning/phases/02-图书馆服务/02-A-SUMMARY.md`
