---
phase: "01-系统服务"
plan: "A"
type: "execute"
wave: 1
depends_on: []
files_modified:
  - "backend/pom.xml"
  - "backend/src/main/java/com/library/LibraryApplication.java"
  - "backend/src/main/resources/application.yml"
  - "frontend/package.json"
  - "frontend/vite.config.js"
  - "frontend/src/main.js"
  - "frontend/src/App.vue"
  - "frontend/src/router/index.js"
  - "frontend/src/stores/user.js"
  - "frontend/src/stores/permission.js"
  - "frontend/src/api/request.js"
  - "sql/init.sql"
autonomous: true
requirements:
  - "AUTH-01"
  - "AUTH-02"
  - "AUTH-03"
  - "USER-01"
  - "USER-05"
  - "AUTHZ-01"
  - "AUTHZ-02"
  - "AUTHZ-03"
  - "AUTHZ-04"
must_haves:
  truths:
    - "后端项目可以启动并注册到 Nacos"
    - "前端项目可以启动并访问登录页"
    - "数据库表结构已创建且包含初始数据"
    - "Spring Security 配置了 JWT 认证过滤器"
  artifacts:
    - path: "backend/pom.xml"
      provides: "Maven 依赖（Spring Boot + Security + JWT + MyBatis-Plus + Nacos）"
      min_lines: 30
    - path: "backend/src/main/java/com/library/LibraryApplication.java"
      provides: "Spring Boot 启动类"
    - path: "backend/src/main/resources/application.yml"
      provides: "Nacos 服务注册配置"
    - path: "frontend/package.json"
      provides: "npm 依赖（Vue 3 + Element UI + Pinia + Axios + Vue Router）"
    - path: "frontend/vite.config.js"
      provides: "Vite 开发服务器配置（代理 /api 到后端）"
    - path: "sql/init.sql"
      provides: "数据库初始化脚本（5 张表 + 初始数据）"
  key_links:
    - from: "frontend/vite.config.js"
      to: "backend application.yml"
      via: "proxy /api -> http://localhost:8080"
    - from: "LibraryApplication.java"
      to: "Nacos"
      via: "spring-cloud-starter-alibaba-nacos-discovery"
---

<objective>
搭建项目骨架：后端 Spring Boot 项目结构 + Nacos 服务注册、前端 Vue 3 项目结构、数据库表结构、Spring Security + JWT 基础配置。
</objective>

<execution_context>
@$HOME/.claude/get-shit-done/workflows/execute-plan.md
@$HOME/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/phases/01-系统服务/SKELETON.md
@.planning/phases/01-系统服务/01-CONTEXT.md
</context>

<tasks>

<task type="auto">
  <name>Task 1: 后端项目结构与依赖</name>
  <files>backend/pom.xml, backend/src/main/java/com/library/LibraryApplication.java, backend/src/main/resources/application.yml</files>
  <action>
创建 Spring Boot 后端项目结构，包含：

**backend/pom.xml** — Maven 依赖（按以下 groupId/artifactId，注意版本兼容）：
- spring-boot-starter-web (3.2.x)
- spring-boot-starter-security
- mybatis-plus-boot-starter (3.5.x)
- mysql-connector-j (8.x)
- io.jsonwebtoken:jjwt-api:0.11.5 + jjwt-impl + jjwt-jackson
- com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery
- org.projectlombok:lombok (provided)
- cn.hutool:hutool-all (5.x)

**backend/src/main/java/com/library/LibraryApplication.java** — 启动类：
```java
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.library.mapper")
public class LibraryApplication {
    public static void main(String[] args) { SpringApplication.run(LibraryApplication.class, args); }
}
```

**backend/src/main/resources/application.yml** — 配置：
```yaml
server:
  port: 8080
spring:
  application:
    name: library-system
  datasource:
    url: jdbc:mysql://localhost:3306/library?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: public
        group: DEFAULT_GROUP
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

创建以下包目录结构（暂不填内容，Plan B/C 会填充）：
- com.library.entity（空）
- com.library.mapper（空）
- com.library.service（空）
- com.library.controller（空）
- com.library.security（空，Plan B 创建）
- com.library.config（空，Plan B 创建）
</action>
  <verify>
<automated>cd backend && mvn compile -q 2>&1 | head -20</automated>
  </verify>
  <done>backend/pom.xml 包含所有依赖，LibraryApplication.java 可编译，application.yml 配置 Nacos 注册</done>
</task>

<task type="auto">
  <name>Task 2: 前端项目结构与依赖</name>
  <files>frontend/package.json, frontend/vite.config.js, frontend/src/main.js, frontend/src/App.vue, frontend/src/router/index.js, frontend/src/stores/user.js, frontend/src/stores/permission.js, frontend/src/api/request.js</files>
  <action>
创建 Vue 3 前端项目结构，包含：

**frontend/package.json** — 依赖：
```json
{
  "name": "library-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite --host 0.0.0.0",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.5.0",
    "@element-plus/icons-vue": "^2.3.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

**frontend/vite.config.js** — Vite 配置：
```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**frontend/src/main.js** — 入口：
```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

**frontend/src/App.vue** — 根组件：
```vue
<template>
  <router-view />
</template>
```

**frontend/src/router/index.js** — 路由（登录页 + 主框架，骨架阶段硬编码）：
```js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: '/dashboard', name: 'Dashboard', component: () => import('../views/dashboard/Index.vue') },
      { path: '/system/user', name: 'UserManage', component: () => import('../views/system/UserManage.vue') },
      { path: '/system/role', name: 'RoleManage', component: () => import('../views/system/RoleManage.vue') },
      { path: '/system/menu', name: 'MenuManage', component: () => import('../views/system/MenuManage.vue') }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

**frontend/src/stores/user.js** — Pinia 用户状态（空实现，Plan B 完善）：
```js
import { defineStore } from 'pinia'
export const useUserStore = defineStore('user', {
  state: () => ({ token: '', userInfo: null }),
  actions: {}
})
```

**frontend/src/stores/permission.js** — Pinia 权限状态（骨架阶段硬编码菜单）：
```js
import { defineStore } from 'pinia'
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [
      { path: '/dashboard', name: 'Dashboard', meta: { title: '首页' } },
      { path: '/system', name: 'System', meta: { title: '系统管理' }, children: [
        { path: '/system/user', name: 'UserManage', meta: { title: '用户管理', perms: 'user:list' } },
        { path: '/system/role', name: 'RoleManage', meta: { title: '角色管理', perms: 'role:list' } },
        { path: '/system/menu', name: 'MenuManage', meta: { title: '菜单管理', perms: 'menu:list' } }
      ]}
    ]
  }),
  actions: {}
})
```

**frontend/src/api/request.js** — Axios 封装（携带 Cookie）：
```js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true  // 关键：携带 Cookie
})

service.interceptors.response.use(
  response => response.data,
  error => {
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service
```
</action>
  <verify>
<automated>cd frontend && npm install --silent 2>&1 | tail -5 && npm run build --silent 2>&1 | tail -10</automated>
  </verify>
  <done>frontend/package.json 包含所有依赖，npm install 成功，npm run build 成功</done>
</task>

<task type="auto">
  <name>Task 3: 数据库初始化脚本</name>
  <files>sql/init.sql</files>
  <action>
创建 sql/init.sql，包含所有表结构和初始数据：

```sql
CREATE DATABASE IF NOT EXISTS library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library;

-- 用户表
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  nickname VARCHAR(50) COMMENT '昵称',
  role_id BIGINT COMMENT '关联角色ID',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名',
  description VARCHAR(200) COMMENT '描述',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0为一级菜单',
  path VARCHAR(200) COMMENT '路由路径',
  component VARCHAR(200) COMMENT '组件路径',
  icon VARCHAR(50) COMMENT '图标',
  sort INT DEFAULT 0 COMMENT '排序',
  perms VARCHAR(100) COMMENT '权限标识',
  menu_type TINYINT COMMENT '类型：1菜单 2按钮',
  status TINYINT DEFAULT 1 COMMENT '状态',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  INDEX idx_role_id (role_id),
  INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 用户角色关联表（用户可能多角色）
CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_user_id (user_id),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 初始数据
-- 管理员角色
INSERT INTO sys_role (role_name, description) VALUES ('管理员', '系统管理员');
-- 普通用户角色
INSERT INTO sys_role (role_name, description) VALUES ('普通用户', '普通用户');

-- admin / admin123 的 BCrypt 加密结果
INSERT INTO sys_user (username, password, nickname, role_id) VALUES
  ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1);

-- 初始菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
  ('首页', 0, '/dashboard', 'dashboard/Index', 'Home', 0, NULL, 1),
  ('系统管理', 0, '/system', NULL, 'Setting', 1, NULL, 1),
  ('用户管理', 2, '/system/user', 'system/UserManage', 'User', 1, 'user:list', 1),
  ('角色管理', 2, '/system/role', 'system/RoleManage', 'Role', 2, 'role:list', 1),
  ('菜单管理', 2, '/system/menu', 'system/MenuManage', 'Menu', 3, 'menu:list', 1);

-- 管理员拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;

-- 管理员拥有管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
```
</action>
  <verify>
<automated>mysql -uroot -proot -e "source sql/init.sql" 2>&1 | grep -i error || echo "SQL executed successfully"</automated>
  </verify>
  <done>sql/init.sql 存在且可执行，5 张表创建成功，admin 用户和初始菜单数据存在</done>
</task>

</tasks>

<threat_model>
## Trust Boundaries

| Boundary | Description |
|----------|-------------|
| client → backend | JWT Token 跨域传递，Cookie HttpOnly |
| backend → MySQL | JDBC 连接，密码 BCrypt 加密存储 |
| backend → Nacos | 服务注册，心跳检测 |

## STRIDE Threat Register

| Threat ID | Category | Component | Disposition | Mitigation Plan |
|-----------|----------|-----------|-------------|-----------------|
| T-01-01 | Information Disclosure | init.sql | mitigate | admin 密码使用 BCrypt 加密，不明文存储 |
| T-01-02 | Tampering | init.sql | mitigate | MySQL 权限控制，应用使用最小权限账户 |
| T-01-03 | Denial of Service | Nacos | accept | 单体部署演示环境，心跳间隔可接受 |
</threat_model>

<verification>
- [ ] `cd backend && mvn compile` 无报错
- [ ] `cd frontend && npm install && npm run build` 成功
- [ ] `mysql -uroot -proot -e "USE library; SHOW TABLES;"` 显示 5 张表
- [ ] `mysql -uroot -proot -e "USE library; SELECT username FROM sys_user;"` 显示 admin
</verification>

<success_criteria>
1. backend 目录包含完整的 Maven 项目结构（pom.xml + 源码目录）
2. frontend 目录包含完整的 Vue 3 项目结构（package.json + 源码目录）
3. sql/init.sql 包含 5 张表的 DDL 和初始数据 DML
4. backend 可通过 `mvn spring-boot:run` 启动并注册到 Nacos
5. frontend 可通过 `npm run dev` 启动并访问登录页
</success_criteria>

<output>
After completion, create `.planning/phases/01-系统服务/01-A-SUMMARY.md`
</output>
