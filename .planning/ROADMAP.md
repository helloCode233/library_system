# ROADMAP: AI智能图书管理系统

**Created:** 2026-05-09
**Granularity:** Coarse
**Project Mode:** mvp

## Phase Summary

| # | Phase | Goal | Requirements | Success Criteria |
|---|-------|------|--------------|------------------|
| 1 | 系统服务 | 用户认证 + RBAC 动态权限 | AUTH, USER, ROLE, MENU, AUTHZ | 登录、动态菜单、权限控制 |
| 2 | 图书馆服务 | 图书 + 借阅业务 | CATE, BOOK, BORROW | CRUD、借书还书、借阅记录 |

---

## Phase 1: 系统服务

**Goal:** 完成用户认证 + RBAC 动态权限管理

**Mode:** mvp

**Requirements:** AUTH-01, AUTH-02, AUTH-03, AUTH-04, AUTH-05, USER-01, USER-02, USER-03, USER-04, USER-05, ROLE-01, ROLE-02, ROLE-03, ROLE-04, ROLE-05, MENU-01, MENU-02, MENU-03, MENU-04, MENU-05, MENU-06, AUTHZ-01, AUTHZ-02, AUTHZ-03, AUTHZ-04

**Success Criteria:**
1. 用户可以使用用户名密码注册并登录，获得 JWT Token
2. 前端登录后能根据用户角色动态渲染侧边栏菜单
3. 管理员可以对用户、角色、菜单进行增删改查
4. 管理员可以为角色分配菜单权限
5. 未授权用户访问 API 返回 403

**Backend:**
- Spring Boot + Nacos（服务注册）
- MySQL 数据库
- JWT Token 认证
- RBAC 权限模型

**Frontend:**
- Vue.js + Element UI
- 登录页面
- 用户管理页面
- 角色管理页面
- 菜单管理页面（树形）
- 动态侧边栏

---

## Phase 2: 图书馆服务

**Goal:** 完成图书分类、图书管理、借阅管理

**Mode:** mvp

**Requirements:** CATE-01, CATE-02, CATE-03, CATE-04, CATE-05, BOOK-01, BOOK-02, BOOK-03, BOOK-04, BOOK-05, BOOK-06, BORROW-01, BORROW-02, BORROW-03, BORROW-04, BORROW-05, BORROW-06

**Success Criteria:**
1. 管理员可以管理图书分类
2. 管理员可以增删改查图书
3. 用户可以搜索和浏览图书
4. 用户可以借书和还书
5. 用户可以查看自己的借阅记录
6. 借书时库存足够检查，还书时更新状态

**Backend:**
- 图书分类 CRUD
- 图书 CRUD + 搜索
- 借阅记录管理（借书、还书、状态更新）

**Frontend:**
- 分类管理页面
- 图书管理页面（列表、搜索）
- 图书详情页面
- 借书/还书功能
- 借阅记录页面

---

## Project State

**Current Phase:** Ready to start Phase 1

**Next Step:** `/gsd-discuss-phase 1`

---
*Last updated: 2026-05-09 after roadmap creation*

### Phase 02.1: 添加一些ai相关的功能 (INSERTED)

**Goal:** 添加 AI 智能搜索、图书推荐、借阅洞察功能
**Requirements:** AI-01, AI-02, AI-03
**Depends on:** Phase 2
**Plans:** 3/3 plans complete

Plans:
- [x] 02.1-01-PLAN.md — AI 基础设施 + 智能搜索（TF-IDF 语义搜索）
- [x] 02.1-02-PLAN.md — AI 图书推荐（内容相似度 + 借阅历史推荐）
- [x] 02.1-03-PLAN.md — AI 借阅洞察（热门排行 + 趋势 + 分类分布）
