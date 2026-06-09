# Requirements: 图书馆管理系统

**Defined:** 2026-05-09
**Core Value:** 微服务架构图书馆管理系统，包含用户认证、RBAC 动态权限管理和图书馆核心业务

## v1 Requirements

### 用户认证 (AUTH)

- [ ] **AUTH-01**: 用户可以使用用户名密码注册账号
- [ ] **AUTH-02**: 用户可以使用用户名密码登录
- [ ] **AUTH-03**: 用户登录成功后获得 JWT Token
- [ ] **AUTH-04**: 用户可以登出（Token 失效）
- [ ] **AUTH-05**: 前端请求携带 Token 访问受保护资源

### 用户管理 (USER)

- [ ] **USER-01**: 管理员可以创建用户（用户名、密码、昵称、角色）
- [ ] **USER-02**: 管理员可以查看用户列表
- [ ] **USER-03**: 管理员可以编辑用户信息
- [ ] **USER-04**: 管理员可以删除用户
- [ ] **USER-05**: 用户可以查看自己的个人信息

### 角色管理 (ROLE)

- [ ] **ROLE-01**: 管理员可以创建角色（角色名、描述）
- [ ] **ROLE-02**: 管理员可以查看角色列表
- [ ] **ROLE-03**: 管理员可以编辑角色信息
- [ ] **ROLE-04**: 管理员可以删除角色
- [ ] **ROLE-05**: 管理员可以为角色分配菜单权限

### 菜单管理 (MENU)

- [ ] **MENU-01**: 管理员可以添加一级菜单（名称、路径、图标）
- [ ] **MENU-02**: 管理员可以添加子菜单（上/下级关系）
- [ ] **MENU-03**: 管理员可以查看菜单树形结构
- [ ] **MENU-04**: 管理员可以编辑菜单
- [ ] **MENU-05**: 管理员可以删除菜单
- [ ] **MENU-06**: 菜单需要关联到角色后才生效

### 权限控制 (AUTHZ)

- [ ] **AUTHZ-01**: 用户只能访问其角色拥有的菜单
- [ ] **AUTHZ-02**: 前端动态渲染侧边栏菜单（根据用户角色）
- [ ] **AUTHZ-03**: 按钮级别权限控制（是否显示）
- [ ] **AUTHZ-04**: 未授权用户访问 API 返回 403

### 图书管理 (BOOK)

- [ ] **BOOK-01**: 管理员可以添加图书（书名、作者、ISBN、分类、库存）
- [ ] **BOOK-02**: 管理员可以查看图书列表
- [ ] **BOOK-03**: 管理员可以编辑图书信息
- [ ] **BOOK-04**: 管理员可以删除图书
- [ ] **BOOK-05**: 用户可以搜索图书（按书名/作者）
- [ ] **BOOK-06**: 用户可以查看图书详情

### 借阅管理 (BORROW)

- [ ] **BORROW-01**: 用户可以借书（选择图书，系统减库存）
- [ ] **BORROW-02**: 用户可以还书（系统加库存）
- [ ] **BORROW-03**: 用户可以查看自己的借阅记录
- [ ] **BORROW-04**: 管理员可以查看所有借阅记录
- [ ] **BORROW-05**: 借书时检查库存是否足够
- [ ] **BORROW-06**: 还书时更新借阅记录状态

### 图书分类 (CATE)

- [ ] **CATE-01**: 管理员可以添加图书分类
- [ ] **CATE-02**: 管理员可以查看分类列表
- [ ] **CATE-03**: 管理员可以编辑分类
- [ ] **CATE-04**: 管理员可以删除分类
- [ ] **CATE-05**: 图书归属某个分类

### AI 智能功能 (AI)

- [ ] **AI-01**: 用户可以使用自然语言进行AI语义搜索（TF-IDF 智能匹配），结果按相关度排序
- [ ] **AI-02**: 用户可以查看AI图书推荐（基于内容相似度的相似图书推荐 + 基于借阅历史的个性化推荐）
- [ ] **AI-03**: 用户可以查看AI借阅洞察（热门图书排行榜、月度借阅趋势、分类借阅分布、借阅总览统计）

## v2 Requirements

Deferred to future release.

- **AUTH-06**: 邮件找回密码
- **AUTH-07**: OAuth 第三方登录
- **BORROW-07**: 超期罚款计算
- **BORROW-08**: 邮件还书提醒

## Out of Scope

| Feature | Reason |
|---------|--------|
| 分布式事务 | 单体部署，课程要求简化 |
| 服务间熔断限流 | 单体部署，不需要 |
| 邮件/短信通知 | MVP 简化 |
| API 文档自动化 | 时间有限 |
| 单元测试覆盖率 | 课程作业简化 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| AUTH-01 ~ AUTH-05 | Phase 1 | Pending |
| USER-01 ~ USER-05 | Phase 1 | Pending |
| ROLE-01 ~ ROLE-05 | Phase 1 | Pending |
| AUTHZ-01 ~ AUTHZ-04 | Phase 1 | Pending |
| MENU-01 ~ MENU-06 | Phase 1 | Pending |
| CATE-01 ~ CATE-05 | Phase 2 | Pending |
| BOOK-01 ~ BOOK-06 | Phase 2 | Pending |
| BORROW-01 ~ BORROW-06 | Phase 2 | Pending |
| AI-01 ~ AI-03 | Phase 02.1 | Pending |

**Coverage:**
- v1 requirements: 36 total
- Mapped to phases: 36
- Unmapped: 0 ✓

---
*Requirements defined: 2026-05-09*
*Last updated: 2026-06-09 after Phase 02.1 AI requirements defined*
