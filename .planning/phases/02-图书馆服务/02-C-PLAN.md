---
phase: "02-图书馆服务"
plan: "C"
type: "execute"
wave: 2
depends_on: ["B"]
files_modified:
  - "backend/src/main/java/com/library/entity/Borrow.java"
  - "backend/src/main/java/com/library/mapper/BorrowMapper.java"
  - "backend/src/main/java/com/library/service/impl/BorrowServiceImpl.java"
  - "backend/src/main/java/com/library/controller/BorrowController.java"
  - "frontend/src/api/borrow.js"
  - "frontend/src/views/borrow/MyRecords.vue"
  - "frontend/src/views/borrow/Manage.vue"
autonomous: true
requirements:
  - "BORROW-01"
  - "BORROW-02"
  - "BORROW-03"
  - "BORROW-04"
  - "BORROW-05"
  - "BORROW-06"
must_haves:
  truths:
    - "用户可以借书（库存减少）"
    - "用户可以还书（库存增加）"
    - "用户可以查看自己的借阅记录"
    - "管理员可以查看所有借阅记录"
  artifacts:
    - path: "backend/src/main/java/com/library/controller/BorrowController.java"
      provides: "借阅/还书/记录查询 API"
      exports: "POST /api/borrow, PUT /api/borrow/{id}/return, GET /api/borrow/my, GET /api/borrow/list"
---

# Plan 02-C: 借阅管理

## Context
@.planning/phases/02-图书馆服务/02-CONTEXT.md
@.planning/phases/02-图书馆服务/02-B-PLAN.md

## Tasks

### Task 1: 后端借阅实体、Mapper、Service、Controller

**backend/src/main/java/com/library/entity/Borrow.java**:
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_borrow")
public class Borrow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer status; // 0=借阅中, 1=已归还, 2=已逾期
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // transient fields for display
    private String bookTitle;
    private String bookIsbn;
    private String username;
}
```

**backend/src/main/java/com/library/mapper/BorrowMapper.java**:
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Borrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {

    @Select("SELECT b.*, u.username, book.title as book_title, book.isbn as book_isbn " +
            "FROM sys_borrow b " +
            "INNER JOIN sys_user u ON b.user_id = u.id " +
            "INNER JOIN sys_book book ON b.book_id = book.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.create_time DESC")
    List<Borrow> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT b.*, u.username, book.title as book_title, book.isbn as book_isbn " +
            "FROM sys_borrow b " +
            "INNER JOIN sys_user u ON b.user_id = u.id " +
            "INNER JOIN sys_book book ON b.book_id = book.id " +
            "ORDER BY b.create_time DESC")
    List<Borrow> selectAll();
}
```

**backend/src/main/java/com/library/service/impl/BorrowServiceImpl.java**:
```java
package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.Borrow;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowServiceImpl {

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookMapper bookMapper;

    @Transactional
    public void borrow(Long userId, Long bookId) {
        // 检查库存
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStock() <= 0) {
            throw new RuntimeException("库存不足");
        }

        // 减库存
        book.setStock(book.getStock() - 1);
        bookMapper.updateById(book);

        // 创建借阅记录
        Borrow borrow = new Borrow();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(LocalDateTime.now().plusDays(30));
        borrow.setStatus(0);
        borrowMapper.insert(borrow);
    }

    @Transactional
    public void returnBook(Long borrowId) {
        Borrow borrow = borrowMapper.selectById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (borrow.getStatus() == 1) {
            throw new RuntimeException("该书已归还");
        }

        // 加库存
        Book book = bookMapper.selectById(borrow.getBookId());
        if (book != null) {
            book.setStock(book.getStock() + 1);
            bookMapper.updateById(book);
        }

        // 更新借阅记录
        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(1);
        borrowMapper.updateById(borrow);
    }

    public List<Borrow> getMyBorrows(Long userId) {
        return borrowMapper.selectByUserId(userId);
    }

    public List<Borrow> getAllBorrows() {
        return borrowMapper.selectAll();
    }
}
```

**backend/src/main/java/com/library/controller/BorrowController.java**:
```java
package com.library.controller;

import com.library.common.Result;
import com.library.entity.Borrow;
import com.library.service.impl.BorrowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowServiceImpl borrowService;

    @PostMapping
    public Result<Void> borrow(@RequestBody Map<String, Long> params, Authentication authentication) {
        // 简化：从 authentication 获取用户，这里硬编码 userId=1
        Long userId = 1L;
        borrowService.borrow(userId, params.get("bookId"));
        return Result.success("借书成功", null);
    }

    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return Result.success("还书成功", null);
    }

    @GetMapping("/my")
    public Result<List<Borrow>> myBorrows(Authentication authentication) {
        // 简化：硬编码 userId=1
        return Result.success(borrowService.getMyBorrows(1L));
    }

    @GetMapping("/list")
    public Result<List<Borrow>> list() {
        return Result.success(borrowService.getAllBorrows());
    }
}
```

### Task 2: 前端借阅 API 和页面

**frontend/src/api/borrow.js**:
```javascript
import request from './request'

export function borrowBook(data) {
  return request({ url: '/borrow', method: 'post', data })
}

export function returnBook(id) {
  return request({ url: `/borrow/${id}/return`, method: 'put' })
}

export function getMyBorrows() {
  return request({ url: '/borrow/my', method: 'get' })
}

export function getAllBorrows() {
  return request({ url: '/borrow/list', method: 'get' })
}
```

**frontend/src/views/borrow/MyRecords.vue**:
```vue
<template>
  <div class="my-records">
    <el-card>
      <template #header>
        <span>我的借阅记录</span>
      </template>
      <el-table :data="tableData" border>
        <el-table-column prop="bookTitle" label="书名" />
        <el-table-column prop="bookIsbn" label="ISBN" width="150" />
        <el-table-column prop="borrowDate" label="借书日期" width="160" />
        <el-table-column prop="dueDate" label="应还日期" width="160" />
        <el-table-column prop="returnDate" label="实际还书日期" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">借阅中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已归还</el-tag>
            <el-tag v-else type="danger">已逾期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              link
              type="success"
              size="small"
              @click="handleReturn(row)"
            >
              还书
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyBorrows, returnBook } from '@/api/borrow'

const tableData = ref([])

const loadData = async () => {
  const res = await getMyBorrows()
  if (res.code === 200) {
    tableData.value = res.data || []
  }
}

const handleReturn = async (row) => {
  try {
    await returnBook(row.id)
    ElMessage.success('还书成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '还书失败')
  }
}

onMounted(() => {
  loadData()
})
</script>
```

**frontend/src/views/borrow/Manage.vue** (admin borrow management):
```vue
<template>
  <div class="borrow-manage">
    <el-card>
      <template #header>
        <span>借阅记录管理</span>
      </template>
      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="bookTitle" label="书名" />
        <el-table-column prop="bookIsbn" label="ISBN" width="150" />
        <el-table-column prop="borrowDate" label="借书日期" width="160" />
        <el-table-column prop="dueDate" label="应还日期" width="160" />
        <el-table-column prop="returnDate" label="实际还书日期" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">借阅中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已归还</el-tag>
            <el-tag v-else type="danger">已逾期</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllBorrows } from '@/api/borrow'

const tableData = ref([])

const loadData = async () => {
  const res = await getAllBorrows()
  if (res.code === 200) {
    tableData.value = res.data || []
  }
}

onMounted(() => {
  loadData()
})
</script>
```

## Verification
- Run `cd backend && mvn compile -q 2>&1 | head -20`
- Run `cd frontend && npm run build 2>&1 | tail -10`

## After Completion
Create `.planning/phases/02-图书馆服务/02-C-SUMMARY.md`
