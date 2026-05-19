# 图书馆管理系统

基于 Spring Cloud + Nacos 的微服务架构图书馆管理系统，包含用户认证、RBAC 动态权限管理和图书馆核心业务。

## 技术栈

**后端**
- Spring Boot 3.2
- Spring Security + JWT
- MyBatis-Plus
- Nacos（服务注册与发现）
- MySQL 8

**前端**
- Vue 3
- Element Plus
- Pinia（状态管理）
- Axios
- Vue Router

## 项目结构

```
web-end/
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/com/library/
│   │   ├── common/          # 通用响应封装
│   │   ├── config/          # 配置类（Security、CORS）
│   │   ├── controller/      # REST API 控制器
│   │   │   └── system/      # 系统管理（用户/角色/菜单/分类）
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── mapper/          # MyBatis-Plus Mapper
│   │   ├── security/         # JWT 认证过滤器
│   │   └── service/          # 业务逻辑
│   └── src/main/resources/
│       └── application.yml   # 配置文件
├── frontend/                 # Vue 3 前端
│   └── src/
│       ├── api/             # API 请求封装
│       ├── stores/           # Pinia 状态管理
│       ├── views/            # 页面组件
│       └── router/           # 路由配置
├── sql/
│   └── init.sql             # 数据库初始化脚本
└── README.md
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -uroot -proot < sql/init.sql
```

### 2. 启动 Nacos（服务注册）

```bash
docker run --name nacos -d -p 8848:8848 -p 9848:9848 nacos/nacos-server
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址: http://localhost:8080

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址: http://localhost:5173

### 5. 登录

- **用户名:** admin
- **密码:** admin123

## 功能模块

### 系统管理（Phase 1）

| 模块 | 功能 |
|------|------|
| 用户管理 | 增删改查用户、分配角色 |
| 角色管理 | 增删改查角色、分配菜单权限 |
| 菜单管理 | 树形菜单增删改查 |
| 认证 | 注册、登录、登出、JWT Token |

### 图书馆业务（Phase 2）

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
| sys_menu | 菜单表 |
| sys_user_role | 用户角色关联表 |
| sys_role_menu | 角色菜单关联表 |
| sys_category | 图书分类表 |
| sys_book | 图书表 |
| sys_borrow | 借阅记录表 |

## API 接口

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/register | 注册 |
| POST | /api/auth/logout | 登出 |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/list | 用户列表（分页） |
| GET | /api/user/{id} | 获取用户 |
| POST | /api/user | 创建用户 |
| PUT | /api/user | 更新用户 |
| DELETE | /api/user/{id} | 删除用户 |
| GET | /api/user/me | 当前用户信息 |

### 角色管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/role/list | 角色列表 |
| GET | /api/role/all | 所有角色 |
| POST | /api/role | 创建角色 |
| PUT | /api/role | 更新角色 |
| DELETE | /api/role/{id} | 删除角色 |
| PUT | /api/role/{id}/menus | 分配菜单权限 |

### 菜单管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/menu/tree | 菜单树 |
| GET | /api/menu/nav | 用户导航菜单 |
| POST | /api/menu | 创建菜单 |
| PUT | /api/menu | 更新菜单 |
| DELETE | /api/menu/{id} | 删除菜单 |

### 分类管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/category/list | 分类列表 |
| GET | /api/category/all | 所有分类 |
| POST | /api/category | 创建分类 |
| PUT | /api/category | 更新分类 |
| DELETE | /api/category/{id} | 删除分类 |

### 图书管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/book/list | 图书列表（搜索/分页） |
| GET | /api/book/{id} | 图书详情 |
| POST | /api/book | 创建图书 |
| PUT | /api/book | 更新图书 |
| DELETE | /api/book/{id} | 删除图书 |

### 借阅管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/borrow | 借书 |
| PUT | /api/borrow/{id}/return | 还书 |
| GET | /api/borrow/my | 我的借阅记录 |
| GET | /api/borrow/list | 所有借阅记录（管理员） |

## 已知限制

1. `MenuController.nav()` 目前硬编码返回 admin 用户菜单，动态用户菜单待完善
2. 登录返回的角色名为硬编码"管理员"，实际应从数据库查询
3. 借阅/还书接口目前硬编码 userId=1，实际应从 JWT Token 解析

## 课程作业要求

- [x] 微服务架构（Nacos 服务注册与发现）
- [x] 完整可运行的用户权限系统
- [x] 图书馆业务功能
- [x] Vue 前端界面
