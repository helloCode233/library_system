# CONTEXT: Phase 2 - 图书馆服务

**Phase:** 02-图书馆服务
**Created:** 2026-05-09
**Requirements:** CATE-01~CATE-05, BOOK-01~BOOK-06, BORROW-01~BORROW-06

## Overview

Phase 2 implements the library business logic:
- **图书分类 (CATE)**: Category CRUD — categories contain books
- **图书管理 (BOOK)**: Book CRUD + search — books belong to a category
- **借阅管理 (BORROW)**: Borrow/return + records — users borrow books

## Domain Model

```
sys_category (id, name, description, sort, status, deleted, create_time, update_time)
sys_book (id, title, author, isbn, category_id, description, cover_url, stock, total, status, deleted, create_time, update_time)
sys_borrow (id, user_id, book_id, borrow_date, due_date, return_date, status, created_at, updated_at)
  - status: 0=借阅中, 1=已归还, 2=已逾期
```

## Key API Endpoints

### Category (分类)
- `GET /api/category/list` — list categories
- `GET /api/category/all` — all categories (for select)
- `GET /api/category/{id}` — get one
- `POST /api/category` — create
- `PUT /api/category` — update
- `DELETE /api/category/{id}` — delete

### Book (图书)
- `GET /api/book/list` — list/search books (params: title, author, categoryId, pageNum, pageSize)
- `GET /api/book/{id}` — book detail
- `POST /api/book` — create (admin)
- `PUT /api/book` — update (admin)
- `DELETE /api/book/{id}` — delete (admin)

### Borrow (借阅)
- `POST /api/borrow` — borrow a book (decrease stock, create record)
- `PUT /api/borrow/{id}/return` — return a book (increase stock, update record)
- `GET /api/borrow/my` — my borrow records (user)
- `GET /api/borrow/list` — all records (admin)

## Business Rules

1. Book stock decreases on borrow, increases on return
2. Cannot borrow if stock = 0
3. Return date is 30 days from borrow date
4. Borrow status: 0=借阅中, 1=已归还

## Frontend Pages Needed

- `/category` — Category management (admin)
- `/book` — Book list + search (all users)
- `/book/{id}` — Book detail
- `/borrow/my` — My borrow records
- `/borrow/manage` — All borrow records (admin)

## Technical Notes

- Category name must be unique
- Book ISBN must be unique
- Borrow check stock before confirming
- Use MyBatis-Plus for all CRUD
- Backend port: 8080, Frontend port: 5173
