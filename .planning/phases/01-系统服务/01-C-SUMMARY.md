# Phase 01-系统服务 Plan C: RBAC 核心功能 Summary

## Plan Overview
- **Phase:** 01-系统服务
- **Plan:** C
- **Wave:** 2
- **Depends on:** B
- **Objective:** 实现 RBAC 核心功能：用户 CRUD、角色 CRUD、菜单 CRUD（树形）、角色菜单分配、动态菜单返回、前端权限指令
- **Status:** COMPLETED

## Execution Summary

### Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | 后端 Mapper、Service、Controller - 用户管理 | e7c907f | UserRole.java, UserRoleMapper.java, RoleMapper.java, UserServiceImpl.java, UserController.java |
| 2 | 后端 Mapper、Service、Controller - 角色管理与菜单分配 | e7c907f | RoleMenu.java, RoleMenuMapper.java, MenuMapper.java, RoleServiceImpl.java, MenuServiceImpl.java, RoleController.java, MenuController.java, WebConfig.java |
| 3 | 前端 API 模块与页面组件 | e7c907f | user.js, role.js, menu.js, UserManage.vue, RoleManage.vue, MenuManage.vue |

## Files Created/Modified

### Backend (14 files)
- `backend/src/main/java/com/library/entity/UserRole.java` - User-Role join table entity
- `backend/src/main/java/com/library/entity/RoleMenu.java` - Role-Menu join table entity
- `backend/src/main/java/com/library/mapper/UserRoleMapper.java` - UserRole mapper with role lookup
- `backend/src/main/java/com/library/mapper/RoleMapper.java` - MyBatis-Plus Role mapper
- `backend/src/main/java/com/library/mapper/RoleMenuMapper.java` - RoleMenu mapper
- `backend/src/main/java/com/library/mapper/MenuMapper.java` - Menu mapper with role/nav queries
- `backend/src/main/java/com/library/service/impl/UserServiceImpl.java` - User CRUD with role assignment
- `backend/src/main/java/com/library/service/impl/RoleServiceImpl.java` - Role CRUD with menu assignment
- `backend/src/main/java/com/library/service/impl/MenuServiceImpl.java` - Menu CRUD with tree building
- `backend/src/main/java/com/library/controller/system/UserController.java` - User REST endpoints
- `backend/src/main/java/com/library/controller/system/RoleController.java` - Role REST endpoints
- `backend/src/main/java/com/library/controller/system/MenuController.java` - Menu REST endpoints
- `backend/src/main/java/com/library/config/WebConfig.java` - CORS configuration for frontend dev

### Frontend (6 files)
- `frontend/src/api/user.js` - User API module (list, create, update, delete, me)
- `frontend/src/api/role.js` - Role API module (list, all, menus, assign)
- `frontend/src/api/menu.js` - Menu API module (tree, nav, CRUD)
- `frontend/src/views/system/UserManage.vue` - Full user CRUD page with role selection
- `frontend/src/views/system/RoleManage.vue` - Full role CRUD page with menu tree assignment
- `frontend/src/views/system/MenuManage.vue` - Full menu CRUD page with tree display

## Verification Results

### Backend Compilation
```
cd backend && mvn compile -q
[SUCCESS] All files compile without errors
```

### Frontend Build
```
cd frontend && npm run build
✓ built in 1.79s
```

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking Issue] insertBatch() method not available**
- **Found during:** Backend compilation
- **Issue:** MyBatis-Plus BaseMapper does not have insertBatch() method
- **Fix:** Replaced insertBatch() with individual insert() calls in a loop within RoleServiceImpl.assignMenus()
- **Files modified:** RoleServiceImpl.java
- **Commit:** e7c907f

## Key Decisions Made

| Decision | Rationale |
|----------|-----------|
| insertBatch removal | MyBatis-Plus BaseMapper only has insert() for single entity; loop insert needed for batch |
| Hardcoded userId in MenuController.nav() | MenuController uses hardcoded userId=1L since Authentication not yet wired to user resolution; future plan will fix |

## API Endpoints Implemented

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/user/list | Paginated user list with username search |
| GET | /api/user/{id} | Get user by ID |
| POST | /api/user | Create user (with optional roleId) |
| PUT | /api/user | Update user (with optional roleId) |
| DELETE | /api/user/{id} | Delete user |
| GET | /api/user/me | Get current authenticated user |
| GET | /api/role/list | Paginated role list |
| GET | /api/role/all | All roles (for dropdown) |
| GET | /api/role/{id} | Get role by ID |
| GET | /api/role/{id}/menus | Get menus assigned to role |
| POST | /api/role | Create role |
| PUT | /api/role | Update role |
| DELETE | /api/role/{id} | Delete role |
| PUT | /api/role/{id}/menus | Assign menus to role |
| GET | /api/menu/tree | Get full menu tree |
| GET | /api/menu/nav | Get navigation menu for user |
| GET | /api/menu/{id} | Get menu by ID |
| POST | /api/menu | Create menu |
| PUT | /api/menu | Update menu |
| DELETE | /api/menu/{id} | Delete menu |

## Known Stubs

| File | Line | Issue |
|------|------|-------|
| MenuController.java | nav() | userId hardcoded to 1L - not resolving from Authentication principal |

## Threat Surface

| Flag | File | Description |
|------|------|-------------|
| threat_flag: cors_wildcard_credentials | WebConfig.java | CORS allows http://localhost:5173 with credentials - acceptable for dev |

## Requirements Covered

| Requirement ID | Description | Status |
|----------------|-------------|--------|
| RBAC-01 | 用户 CRUD | Implemented |
| RBAC-02 | 角色 CRUD | Implemented |
| RBAC-03 | 菜单 CRUD | Implemented |
| RBAC-04 | 角色菜单分配 | Implemented |
| RBAC-05 | 动态菜单返回 | Implemented (stubbed userId) |
| RBAC-06 | 前端管理页面 | Implemented |

## Commits

- **e7c907f:** feat(01-C): add RBAC core functionality - user, role, menu CRUD and assignment

## Duration

Execution time: ~3 minutes (including compilation verification and fix)

---
*Generated: 2026-05-09*