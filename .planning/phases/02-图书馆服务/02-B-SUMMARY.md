# Phase 02 Plan B Summary: 图书管理

## Overview
- **Phase:** 02-图书馆服务
- **Plan:** B
- **Plan Type:** execute
- **Wave:** 1
- **Status:** COMPLETED
- **Execution Time:** 2026-05-09T07:20:36Z

## Objective
图书管理完整 CRUD + 搜索功能

## Tasks Completed

| Task | Name | Status | Files |
|------|------|--------|-------|
| 1 | 后端图书 CRUD | DONE | Book.java, BookMapper.java, BookServiceImpl.java, BookController.java |
| 2 | 前端图书 API 和页面 | DONE | book.js, Index.vue, Detail.vue |

## Files Created/Modified

### Backend (4 files)
- `backend/src/main/java/com/library/entity/Book.java` - Book entity with MyBatis-Plus annotations
- `backend/src/main/java/com/library/mapper/BookMapper.java` - Book mapper extending BaseMapper
- `backend/src/main/java/com/library/service/impl/BookServiceImpl.java` - Book service with CRUD + search + stock management
- `backend/src/main/java/com/library/controller/BookController.java` - REST API controller

### Frontend (3 files)
- `frontend/src/api/book.js` - Book API functions (getBookList, getBook, createBook, updateBook, deleteBook)
- `frontend/src/views/book/Index.vue` - Book list page with search, CRUD dialog, and borrow functionality
- `frontend/src/views/book/Detail.vue` - Book detail page with borrow action

## Key Decisions

1. **ISBN Uniqueness Check** - BookServiceImpl.create() checks for duplicate ISBN before inserting
2. **Stock Initialization** - If total is not set, it defaults to stock value on create
3. **Soft Delete** - Using MyBatis-Plus @TableLogic for soft delete on Book entity
4. **Category Integration** - Frontend Index.vue loads categories for filtering and selection dialog

## API Endpoints Exposed

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/book/list | List/search books with pagination |
| GET | /api/book/{id} | Get book by ID |
| POST | /api/book | Create new book |
| PUT | /api/book | Update existing book |
| DELETE | /api/book/{id} | Delete book |

## Verification Results

### Backend Compilation
```
mvn compile -q: PASSED (no errors)
```

### Frontend Build
```
npm run build: PASSED (built in 1.84s)
```

## Dependencies
- Frontend Index.vue imports from `@/api/category` (getAllCategories) and `@/api/borrow` (borrowBook)
- These APIs are expected to be created in other plans (02-C for borrow, category may already exist)

## Requirements Covered
- BOOK-01: 图书基本信息管理
- BOOK-02: 图书搜索功能
- BOOK-03: 图书上架/下架
- BOOK-04: 图书库存管理
- BOOK-05: 图书分类管理
- BOOK-06: 图书详情查看
