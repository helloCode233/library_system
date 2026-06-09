# Phase 1: 系统服务 - Context

**Gathered:** 2026-05-09
**Status:** Ready for planning

<domain>
## Phase Boundary

完成用户认证 + RBAC 动态权限管理。包括：用户注册/登录、JWT Token 认证、用户 CRUD、角色 CRUD、菜单树形管理、动态菜单渲染、按钮级权限控制。交付可运行的后端 API 和 Vue 前端界面。

</domain>

<decisions>
## Implementation Decisions

### 认证与授权
- **D-01:** JWT Token 存储在 Cookie 中（CookieHttpOnly），前端通过 JS 读取
- **D-02:** 默认管理员账户：admin / admin123（需要数据库初始化脚本）
- **D-03:** 登出时 Token 失效（前端删除 Cookie，后端可选校验黑名单）

### 技术栈
- **D-04:** 后端：Spring Boot + Nacos（服务注册）+ MyBatis-Plus + MySQL
- **D-05:** 前端：Vue 3 + Element UI + Pinia（状态管理）
- **D-06:** 认证层：Spring Security + JWT（jjwt 库）

### 前端架构
- **D-07:** 登录页 + 主框架（侧边栏动态菜单）
- **D-08:** 用户管理、角色管理、菜单管理页面（Element UI 表格 + 表单）
- **D-09:** 按钮级权限控制：后端返回权限标识，前端 v-if 控制显示

### 数据库
- **D-10:** 用户表：id, username, password(加密), nickname, role_id, create_time
- **D-11:** 角色表：id, role_name, description, create_time
- **D-12:** 菜单表：id, menu_name, parent_id, path, icon, sort, perms（权限标识）
- **D-13:** 角色-菜单关联表：role_id, menu_id

### 微服务结构
- **D-14:** 单体部署，3 个 service 打包为单个应用
- **D-15:** system-service 处理所有系统管理 API
- **D-16:** library-service 在 Phase 2 处理图书/借阅 API

### Claude's Discretion
- 前端目录结构、组件拆分、API 请求封装方式由规划阶段决定
- Nacos 配置中心的具体 namespace/group 设置由规划阶段决定

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 需求与规划
- `.planning/PROJECT.md` — 项目总体描述、技术栈、微服务规划
- `.planning/REQUIREMENTS.md` — AUTH-01~AUTH-05, USER-01~USER-05, ROLE-01~ROLE-05, MENU-01~MENU-06, AUTHZ-01~AUTHZ-04
- `.planning/ROADMAP.md` — Phase 1 的目标、成功标准、后端/前端技术说明

### 无外部文档
No external specs — requirements fully captured in decisions above.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- 无已有代码（新建项目）

### Established Patterns
- 新建项目，遵循 Spring Boot 最佳实践

### Integration Points
- Phase 2 的 library-service 需要调用 system-service 的用户信息（暂时单体，内部调用）

</code_context>

<specifics>
## Specific Ideas

No specific references — open to standard approaches for UI layouts and component structure.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 1-系统服务*
*Context gathered: 2026-05-09*
