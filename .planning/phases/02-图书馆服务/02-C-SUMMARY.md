# Phase 02 Plan C: 借阅管理 - Execution Summary

**Phase:** 02-图书馆服务
**Plan:** C
**Wave:** 2
**Completed:** 2026-05-09
**Duration:** ~2 minutes

## Objective

实现借阅管理完整功能：借书、还书、借阅记录查询。

## Files Created

### Backend (4 files)

| File | Purpose |
|------|---------|
| `backend/src/main/java/com/library/entity/Borrow.java` | 借阅实体，包含 userId, bookId, dates, status + transient display fields |
| `backend/src/main/java/com/library/mapper/BorrowMapper.java` | Mapper with custom SQL for user borrow list and all borrows (JOIN user + book) |
| `backend/src/main/java/com/library/service/impl/BorrowServiceImpl.java` | Service: borrow() decrements stock, returnBook() increments stock, transactional |
| `backend/src/main/java/com/library/controller/BorrowController.java` | REST API: POST /api/borrow, PUT /api/borrow/{id}/return, GET /api/borrow/my, GET /api/borrow/list |

### Frontend (3 files)

| File | Purpose |
|------|---------|
| `frontend/src/api/borrow.js` | API: borrowBook, returnBook, getMyBorrows, getAllBorrows |
| `frontend/src/views/borrow/MyRecords.vue` | User view: own borrow records with return button |
| `frontend/src/views/borrow/Manage.vue` | Admin view: all borrow records |

## API Endpoints Implemented

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/borrow` | 借书 (body: {bookId}) |
| PUT | `/api/borrow/{id}/return` | 还书 |
| GET | `/api/borrow/my` | 我的借阅记录 |
| GET | `/api/borrow/list` | 所有借阅记录 (admin) |

## Business Rules Applied

- Stock decreases by 1 on borrow
- Stock increases by 1 on return
- Borrow throws if book not found or stock = 0
- Return throws if borrow record not found or already returned
- @Transactional on borrow() and returnBook()

## Verification

**Backend compile:** `mvn compile -q` passed with no output (clean)

**Frontend build:** `npm run build` passed - 1.04 MB main chunk (warning only, expected for MVP)

## Requirements Covered

| ID | Requirement | Status |
|----|-------------|--------|
| BORROW-01 | 借书功能 | Done |
| BORROW-02 | 还书功能 | Done |
| BORROW-03 | 借阅记录查询 | Done |
| BORROW-04 | 管理员查看所有记录 | Done |
| BORROW-05 | 库存管理 | Done (stock decrements/increments) |
| BORROW-06 | 业务规则校验 | Done (stock check, already returned check) |

## Deviations from Plan

None - plan executed exactly as written.

## Self-Check

- [x] Borrow.java exists at correct path
- [x] BorrowMapper.java exists with selectByUserId and selectAll
- [x] BorrowServiceImpl.java exists with borrow/return/getMyBorrows/getAllBorrows
- [x] BorrowController.java exists with all 4 endpoints
- [x] frontend/src/api/borrow.js exists
- [x] frontend/src/views/borrow/MyRecords.vue exists
- [x] frontend/src/views/borrow/Manage.vue exists
- [x] Backend compiles cleanly
- [x] Frontend builds successfully

## Self-Check: PASSED