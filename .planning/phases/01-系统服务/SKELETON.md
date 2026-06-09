# SKELETON.md — Phase 1: 系统服务

**Phase:** 01-系统服务
**Mode:** Walking Skeleton (Phase 1 of new project)
**Created:** 2026-05-09

---

## 1. 项目结构

```
web-end/
├── backend/                          # Spring Boot 后端（单体部署）
│   └── src/main/java/com/library/
│       ├── LibraryApplication.java    # 启动类
│       ├── config/
│       │   └── SecurityConfig.java   # Spring Security 配置
│       ├── controller/
│       │   └── system/               # system-service: 用户/角色/菜单 API
│       │       ├── AuthController.java
│       │       ├── UserController.java
│       │       ├── RoleController.java
│       │       └── MenuController.java
│       ├── service/
│       │   └── system/
│       │       ├── AuthService.java
│       │       ├── UserService.java
│       │       ├── RoleService.java
│       │       └── MenuService.java
│       ├── mapper/
│       │   └── system/
│       │       ├── UserMapper.java
│       │       ├── RoleMapper.java
│       │       └── MenuMapper.java
│       ├── entity/
│       │   ├── User.java
│       │   ├── Role.java
│       │   ├── Menu.java
│       │   └── UserRole.java
│       ├── dto/
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   └── UserDto.java
│       ├── security/
│       │   ├── JwtTokenProvider.java  # JWT 生成/校验
│       │   ├── JwtAuthenticationFilter.java
│       │   └── UserDetailsServiceImpl.java
│       └── common/
│           └── Result.java            # 统一响应封装
├── frontend/                          # Vue 3 前端
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/
│       │   └── index.js              # Vue Router 配置
│       ├── stores/
│       │   ├── user.js               # Pinia: 用户状态
│       │   └── permission.js         # Pinia: 权限/菜单
│       ├── views/
│       │   ├── Login.vue             # 登录页
│       │   ├── Layout.vue            # 主框架（侧边栏+头部）
│       │   ├── system/
│       │   │   ├── UserManage.vue    # 用户管理
│       │   │   ├── RoleManage.vue    # 角色管理
│       │   │   └── MenuManage.vue     # 菜单管理
│       │   └── dashboard/
│       │       └── Index.vue         # 首页
│       ├── api/
│       │   ├── auth.js
│       │   ├── user.js
│       │   ├── role.js
│       │   └── menu.js
│       └── utils/
│           └── request.js            # Axios 封装（含 Token 处理）
└── sql/
    └── init.sql                      # 数据库初始化脚本
```

**关键说明：**
- 后端采用标准 Spring Boot 分层结构（Controller/Service/Mapper/Entity）
- 前端采用 Vue 3 Composition API + Pinia 状态管理
- system-service 和 library-service 代码共存于 backend，但逻辑分离
- Nacos 用于服务注册（单机模式），不启用配置中心

---

## 2. 数据库表设计

### 用户表 (sys_user)
```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  nickname VARCHAR(50) COMMENT '昵称',
  role_id BIGINT COMMENT '关联角色ID',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 角色表 (sys_role)
```sql
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名',
  description VARCHAR(200) COMMENT '描述',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 菜单表 (sys_menu)
```sql
CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0为一级菜单',
  path VARCHAR(200) COMMENT '路由路径',
  component VARCHAR(200) COMMENT '组件路径',
  icon VARCHAR(50) COMMENT '图标',
  sort INT DEFAULT 0 COMMENT '排序',
  perms VARCHAR(100) COMMENT '权限标识（如 user:add）',
  menu_type TINYINT COMMENT '类型：1菜单 2按钮',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
```

### 角色-菜单关联表 (sys_role_menu)
```sql
CREATE TABLE sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  INDEX idx_role_id (role_id),
  INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';
```

### 初始数据
```sql
-- 管理员角色
INSERT INTO sys_role (role_name, description) VALUES ('管理员', '系统管理员');

-- 默认管理员账户（密码 BCrypt 加密：admin123）
INSERT INTO sys_user (username, password, nickname, role_id) VALUES
  ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1);

-- 初始菜单（硬编码一期功能）
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
  ('系统管理', 0, '/system', NULL, 'Setting', 1, NULL, 1),
  ('用户管理', 1, '/system/user', 'system/UserManage', 'User', 1, 'user:list', 1),
  ('角色管理', 1, '/system/role', 'system/RoleManage', 'Role', 2, 'role:list', 1),
  ('菜单管理', 1, '/system/menu', 'system/MenuManage', 'Menu', 3, 'menu:list', 1),
  ('首页', 0, '/dashboard', 'dashboard/Index', 'Home', 0, NULL, 1);

-- 管理员角色拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;
```

---

## 3. 端到端流程（骨架验证路径）

### 注册 → 登录 → 获取用户信息

**Step 1: 注册**
- POST `/api/auth/register`
- Body: `{ "username": "test", "password": "test123", "nickname": "测试用户" }`
- Response: `{ "code": 200, "message": "注册成功" }`

**Step 2: 登录**
- POST `/api/auth/login`
- Body: `{ "username": "admin", "password": "admin123" }`
- Response: `{ "code": 200, "data": { "token": "xxx", "user": { "id": 1, "username": "admin", "nickname": "系统管理员" } } }`
- **Cookie 设置：** `Set-Cookie: token=xxx; HttpOnly; Path=/; Max-Age=86400`

**Step 3: 获取当前用户信息（携带 Cookie）**
- GET `/api/user/me`
- Header: 自动携带 Cookie
- Response: `{ "code": 200, "data": { "id": 1, "username": "admin", "nickname": "系统管理员", "roles": ["管理员"], "menus": [...] } }`

**Step 4: 获取动态菜单**
- GET `/api/menu/nav`
- Response: `{ "code": 200, "data": [ { "name": "系统管理", "children": [...] } ] }`

### 前端骨架验证

1. 打开登录页 `/login`，显示用户名/密码输入框
2. 输入 admin / admin123，点击登录
3. 登录成功后跳转到首页 `/dashboard`
4. 左侧边栏显示"首页"、"系统管理"菜单（含用户/角色/菜单子菜单）
5. 点击"用户管理"，右侧显示用户列表页面

---

## 4. 前端登录页面 + 主框架

### Login.vue（登录页）
- Element UI `el-form` + `el-input`
- 表单验证：用户名、密码必填
- 登录按钮调用 `/api/auth/login`
- 登录成功：保存 token（Cookie）→ 跳转到 `/dashboard`
- 登录失败：显示 `el-message` 错误提示

### Layout.vue（主框架）
- 左侧 `el-menu`：动态渲染菜单（从 `/api/menu/nav` 获取）
- 顶部 `el-header`：显示当前用户昵称 + 登出按钮
- 右侧内容区 `<router-view />`

### 硬编码菜单（骨架阶段）
骨架阶段使用硬编码菜单证明前端可工作，之后替换为动态菜单：

```javascript
// stores/permission.js (骨架阶段)
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 骨架阶段硬编码，正式阶段从 API 获取
    routes: [
      { path: '/dashboard', name: 'Dashboard', component: 'dashboard/Index', meta: { title: '首页' } },
      { path: '/system', name: 'System', meta: { title: '系统管理' }, children: [
        { path: '/system/user', name: 'UserManage', component: 'system/UserManage', meta: { title: '用户管理' } },
        { path: '/system/role', name: 'RoleManage', component: 'system/RoleManage', meta: { title: '角色管理' } },
        { path: '/system/menu', name: 'MenuManage', component: 'system/MenuManage', meta: { title: '菜单管理' } },
      ]}
    ]
  })
})
```

---

## 5. 技术决策记录

| Decision | Value |
|----------|-------|
| 数据库 | MySQL 8.0+，InnoDB 引擎，utf8mb4 字符集 |
| ORM | MyBatis-Plus（简化 CRUD，自动填充时间戳） |
| 认证 | Spring Security + JWT（jjwt 0.11.5） |
| Token 存储 | Cookie（HttpOnly=true），前端通过 document.cookie 读取 |
| 密码加密 | BCrypt（Spring Security 内置） |
| 服务注册 | Nacos（单机模式，仅注册不启用配置中心） |
| 前端状态 | Pinia（用户信息、权限菜单） |
| API 封装 | Axios + 请求拦截器（自动携带 Cookie） |
| 响应格式 | 统一 `Result<T>` 封装 `{ code, message, data }` |

---

## 6. 骨架验收标准

| 验收项 | 成功条件 |
|--------|----------|
| 后端启动 | `mvn spring-boot:run` 无报错，端口 8080 |
| Nacos 注册 | 访问 `http://localhost:8848/nacos`，可见 library-system 服务 |
| 数据库 | `init.sql` 执行成功，5 张表创建，admin 用户存在 |
| 前端启动 | `npm run dev` 无报错，端口 5173 |
| 登录流程 | admin/admin123 登录成功，返回 token |
| Cookie 存储 | 浏览器 DevTools > Application > Cookies 可见 token |
| 主框架渲染 | 登录后左侧显示菜单，右侧显示 dashboard 内容 |
| 用户信息 API | GET /api/user/me 返回当前用户信息 |

---

*Last updated: 2026-05-09 after skeleton design*
