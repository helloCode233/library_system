# AI智能图书管理系统

基于 Spring Cloud + Nacos 的微服务架构AI智能图书管理系统，包含用户认证、RBAC 动态权限管理和图书馆核心业务（图书管理、借阅管理）。

## 技术栈

**后端**
- Spring Boot 3.2
- Spring Cloud Gateway
- MyBatis-Plus
- Nacos（服务注册与发现）
- MySQL 8

**前端**
- Vue 3
- Element Plus
- Pinia（状态管理）
- Axios
- Vue Router

## 架构

```
浏览器 (Vue 前端: 5173)
       │
       ▼
   Gateway (8080)  ── JWT 鉴权 ──>  Auth-Service (8083)
       │                                │
       │ lb://library-system            │ MySQL ── sys_user
       ▼                                │
   Backend (8081)                       └── 签发 JWT，提供 /auth/** 接口
       │
       │ MySQL
       ▼
   sys_user / sys_role / sys_menu / sys_category / sys_book / sys_borrow
```

- **Gateway** 作为唯一入口，拦截所有请求进行 JWT 校验，白名单放行 `/auth/login`、`/auth/register`
- **Auth-Service** 负责用户登录、注册、Token 签发，独立部署于 8083
- **Backend** 负责图书馆核心业务 CRUD，部署于 8081
- 所有服务均注册到 Nacos，Gateway 通过 Nacos 服务发现进行负载均衡路由

## 项目结构

```
web-end/
├── gateway/                   # Spring Cloud Gateway（8080）
│   └── src/main/java/com/library/gateway/
│       ├── GatewayApplication.java
│       ├── filter/
│       │   └── AuthGlobalFilter.java   # JWT 全局鉴权过滤器
│       └── resources/
│           └── application.yml
├── auth-service/              # 认证服务（8083）
│   └── src/main/java/com/library/auth/
│       ├── AuthServiceApplication.java
│       ├── controller/
│       │   └── AuthController.java      # 登录、注册、当前用户
│       ├── dto/                         # LoginRequest / LoginResponse
│       ├── entity/                      # User 实体
│       ├── mapper/                      # UserMapper
│       ├── security/
│       │   └── JwtUtil.java             # JWT 签发工具
│       ├── service/
│       │   └── AuthServiceImpl.java     # 认证业务逻辑
│       └── resources/
│           └── application.yml
├── backend/                   # 图书馆业务服务（8081）
│   └── src/main/java/com/library/
│       ├── LibraryApplication.java
│       ├── common/Result.java
│       ├── config/WebConfig.java        # CORS 配置
│       ├── controller/
│       │   ├── system/                  # UserController / RoleController / MenuController / CategoryController
│       │   ├── BookController.java
│       │   └── BorrowController.java
│       ├── entity/                      # User / Role / Menu / Category / Book / Borrow
│       ├── mapper/                      # MyBatis-Plus Mapper
│       └── service/
│           └── impl/                    # 业务逻辑实现
├── frontend/                 # Vue 3 前端（5173）
│   ├── src/
│   │   ├── api/             # API 请求封装（axios + JWT 拦截器）
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── views/            # 页面组件
│   │   └── router/           # 路由配置
│   └── vite.config.js       # Vite 配置（代理到 Gateway）
├── sql/
│   └── init.sql             # 数据库初始化脚本
├── start.sh                  # 一键启动所有服务
├── stop.sh                   # 一键停止所有服务
└── pom.xml                   # Maven 父 POM
```

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8
- Nacos 2.x（需先启动）

### 1. 初始化数据库

```bash
mysql -uroot -proot < sql/init.sql
```

### 2. 启动 Nacos

Nacos 需在 8848 端口运行。如果本地已有 Nacos，确保已启动；否则通过 Docker 启动：

```bash
docker run --name nacos -d -p 8848:8848 -p 9848:9848 nacos/nacos-server
```

### 3. 一键启动

```bash
chmod +x start.sh stop.sh
./start.sh
```

脚本将依次启动：auth-service (8083) → backend (8081) → gateway (8080) → frontend (5173)

### 4. 手动启动（可选）

```bash
# 启动认证服务
mvn -pl auth-service spring-boot:run

# 启动业务服务
mvn -pl backend spring-boot:run

# 启动网关
mvn -pl gateway spring-boot:run

# 启动前端
cd frontend && npm install && npm run dev
```

### 5. 登录

访问 http://localhost:5173

- **用户名:** admin
- **密码:** admin123

## 功能模块

### 系统管理

| 模块 | 功能 |
|------|------|
| 用户管理 | 增删改查用户、分配角色 |
| 角色管理 | 增删改查角色、分配菜单权限 |
| 菜单管理 | 树形菜单增删改查（目录 / 菜单 / 按钮） |
| 认证 | 注册、登录、JWT Token（由 auth-service + Gateway 过滤器实现） |

### 图书馆业务

| 模块 | 功能 |
|------|------|
| 分类管理 | 图书分类增删改查 |
| 图书管理 | 图书增删改查、按书名/作者/分类搜索 |
| 借阅管理 | 借书（减库存）、还书（加库存）、借阅记录查询 |

## 数据库表

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_menu | 菜单表（树形结构） |
| sys_user_role | 用户角色关联表 |
| sys_role_menu | 角色菜单关联表 |
| sys_category | 图书分类表 |
| sys_book | 图书表 |
| sys_borrow | 借阅记录表 |

## API 接口

### 认证（auth-service: `/auth/**`）

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | /auth/login | 登录，返回 JWT Token | 白名单 |
| POST | /auth/register | 注册 | 白名单 |
| GET | /auth/me | 当前用户信息 | JWT |

### 用户管理（`/api/user`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/list | 用户列表（分页） |
| GET | /api/user/{id} | 获取用户 |
| POST | /api/user | 创建用户 |
| PUT | /api/user | 更新用户 |
| DELETE | /api/user/{id} | 删除用户 |
| GET | /api/user/me | 当前用户信息 |

### 角色管理（`/api/role`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/role/list | 角色列表（分页） |
| GET | /api/role/all | 所有角色 |
| GET | /api/role/{id} | 获取角色 |
| GET | /api/role/{id}/menus | 获取角色菜单 |
| POST | /api/role | 创建角色 |
| PUT | /api/role | 更新角色 |
| DELETE | /api/role/{id} | 删除角色 |
| PUT | /api/role/{id}/menus | 分配菜单权限 |

### 菜单管理（`/api/menu`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/menu/tree | 菜单树 |
| GET | /api/menu/nav | 用户导航菜单 |
| GET | /api/menu/{id} | 获取菜单 |
| POST | /api/menu | 创建菜单 |
| PUT | /api/menu | 更新菜单 |
| DELETE | /api/menu/{id} | 删除菜单 |

### 分类管理（`/api/category`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/category/list | 分类列表（分页） |
| GET | /api/category/all | 所有分类 |
| GET | /api/category/{id} | 获取分类 |
| POST | /api/category | 创建分类 |
| PUT | /api/category | 更新分类 |
| DELETE | /api/category/{id} | 删除分类 |

### 图书管理（`/api/book`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/book/list | 图书列表（搜索/分页） |
| GET | /api/book/{id} | 图书详情 |
| POST | /api/book | 创建图书 |
| PUT | /api/book | 更新图书 |
| DELETE | /api/book/{id} | 删除图书 |

### 借阅管理（`/api/borrow`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/borrow | 借书 |
| PUT | /api/borrow/{id}/return | 还书 |
| GET | /api/borrow/my | 我的借阅记录 |
| GET | /api/borrow/list | 所有借阅记录（管理员） |

## 已知限制

1. 登录返回的角色名目前硬编码为"管理员"，实际应从数据库查询用户-角色关联
2. 借阅/还书接口的 userId 从 Gateway 传递的 `X-User-Id` 头获取，实际用户名需转换为用户 ID

## 课程作业要求

- [x] 微服务架构（Nacos 服务注册与发现，3 个微服务 + Gateway）
- [x] 完整可运行的用户权限系统（RBAC 动态菜单权限）
- [x] 图书馆业务功能（图书管理、借阅管理）
- [x] Vue 前端界面（含 JWT 拦截器、路由守卫）
