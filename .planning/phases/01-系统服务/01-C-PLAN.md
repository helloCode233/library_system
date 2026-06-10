---
phase: "01-系统服务"
plan: "C"
type: "execute"
wave: 2
depends_on: ["B"]
files_modified:
  - "backend/src/main/java/com/library/entity/UserRole.java"
  - "backend/src/main/java/com/library/mapper/RoleMapper.java"
  - "backend/src/main/java/com/library/mapper/MenuMapper.java"
  - "backend/src/main/java/com/library/mapper/UserRoleMapper.java"
  - "backend/src/main/java/com/library/service/impl/UserServiceImpl.java"
  - "backend/src/main/java/com/library/service/impl/RoleServiceImpl.java"
  - "backend/src/main/java/com/library/service/impl/MenuServiceImpl.java"
  - "backend/src/main/java/com/library/controller/system/UserController.java"
  - "backend/src/main/java/com/library/controller/system/RoleController.java"
  - "backend/src/main/java/com/library/controller/system/MenuController.java"
  - "backend/src/main/java/com/library/config/WebConfig.java"
  - "frontend/src/views/Layout.vue"
  - "frontend/src/views/dashboard/Index.vue"
  - "frontend/src/views/system/UserManage.vue"
  - "frontend/src/views/system/RoleManage.vue"
  - "frontend/src/views/system/MenuManage.vue"
  - "frontend/src/api/user.js"
  - "frontend/src/api/role.js"
  - "frontend/src/api/menu.js"
autonomous: true
requirements:
  - "USER-01"
  - "USER-02"
  - "USER-03"
  - "USER-04"
  - "USER-05"
  - "ROLE-01"
  - "ROLE-02"
  - "ROLE-03"
  - "ROLE-04"
  - "ROLE-05"
  - "MENU-01"
  - "MENU-02"
  - "MENU-03"
  - "MENU-04"
  - "MENU-05"
  - "MENU-06"
  - "AUTHZ-01"
  - "AUTHZ-02"
  - "AUTHZ-03"
  - "AUTHZ-04"
must_haves:
  truths:
    - "管理员可以增删改查用户"
    - "管理员可以增删改查角色"
    - "管理员可以增删改查菜单（树形结构）"
    - "管理员可以为角色分配菜单权限"
    - "用户只能访问其角色拥有的菜单（动态菜单）"
    - "未授权用户访问 API 返回 403"
  artifacts:
    - path: "backend/src/main/java/com/library/controller/system/UserController.java"
      provides: "用户 CRUD API"
      exports: "GET/POST/PUT/DELETE /api/user"
    - path: "backend/src/main/java/com/library/controller/system/RoleController.java"
      provides: "角色 CRUD + 菜单分配 API"
      exports: "GET/POST/PUT/DELETE /api/role, PUT /api/role/{id}/menus"
    - path: "backend/src/main/java/com/library/controller/system/MenuController.java"
      provides: "菜单 CRUD + 导航 API"
      exports: "GET/POST/PUT/DELETE /api/menu, GET /api/menu/nav, GET /api/menu/tree"
    - path: "frontend/src/views/Layout.vue"
      provides: "主框架（动态侧边栏 + 顶部栏）"
    - path: "frontend/src/views/system/UserManage.vue"
      provides: "用户管理页面"
    - path: "frontend/src/views/system/RoleManage.vue"
      provides: "角色管理页面（含菜单权限分配）"
    - path: "frontend/src/views/system/MenuManage.vue"
      provides: "菜单管理页面（树形）"
  key_links:
    - from: "Layout.vue"
      to: "MenuController /api/menu/nav"
      via: "onMounted 获取动态菜单"
    - from: "RoleController"
      to: "sys_role_menu"
      via: "角色菜单关联表读写"
    - from: "MenuController"
      to: "sys_menu"
      via: "MyBatis-Plus 树形查询"
    - from: "UserController"
      to: "sys_user"
      via: "MyBatis-Plus CRUD"
---

<objective>
实现 RBAC 核心功能：用户 CRUD、角色 CRUD、菜单 CRUD（树形）、角色菜单分配、动态菜单返回、前端权限指令。
</objective>

<execution_context>
@$HOME/.claude/get-shit-done/workflows/execute-plan.md
@$HOME/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/phases/01-系统服务/SKELETON.md
@.planning/phases/01-系统服务/01-B-PLAN.md
</context>

<interfaces>
From backend/src/main/resources/application.yml:
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

From sql/init.sql:
```sql
-- 5 张表：sys_user, sys_role, sys_menu, sys_role_menu, sys_user_role
-- admin 用户 id=1, 管理员角色 id=1
-- 初始菜单：首页、系统管理（含用户/角色/菜单子菜单）
```
</interfaces>

<tasks>

<task type="auto">
  <name>Task 1: 后端 Mapper、Service、Controller — 用户管理</name>
  <files>backend/src/main/java/com/library/entity/UserRole.java, backend/src/main/java/com/library/mapper/RoleMapper.java, backend/src/main/java/com/library/mapper/UserRoleMapper.java, backend/src/main/java/com/library/service/impl/UserServiceImpl.java, backend/src/main/java/com/library/controller/system/UserController.java, backend/src/main/java/com/library/mapper/UserMapper.java</files>
  <action>
创建用户管理完整后端实现：

**backend/src/main/java/com/library/entity/UserRole.java**：
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_role")
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long roleId;
}
```

**backend/src/main/java/com/library/mapper/UserRoleMapper.java**：
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
```

**backend/src/main/java/com/library/mapper/RoleMapper.java**：
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
```

**backend/src/main/java/com/library/service/impl/UserServiceImpl.java**：
```java
package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.User;
import com.library.entity.UserRole;
import com.library.mapper.UserMapper;
import com.library.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<User> list(Integer pageNum, Integer pageSize, String username) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Transactional
    public void create(User user, Long roleId) {
        // 检查用户名唯一
        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);

        // 关联角色
        if (roleId != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    @Transactional
    public void update(User user, Long roleId) {
        userMapper.updateById(user);
        // 更新角色关联：先删后插
        if (roleId != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
    }

    public User getCurrentUser(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }
}
```

**backend/src/main/java/com/library/controller/system/UserController.java**：
```java
package com.library.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.User;
import com.library.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        return Result.success(userService.list(pageNum, pageSize, username));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody User user,
                               @RequestParam(required = false) Long roleId) {
        userService.create(user, roleId);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody User user,
                               @RequestParam(required = false) Long roleId) {
        userService.update(user, roleId);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }
}
```
</action>
  <verify>
<automated>cd backend && mvn compile -q 2>&1 | grep -i error | head -10</automated>
  </verify>
  <done>UserController, UserServiceImpl 可编译</done>
</task>

<task type="auto">
  <name>Task 2: 后端 Mapper、Service、Controller — 角色管理与菜单分配</name>
  <files>backend/src/main/java/com/library/mapper/MenuMapper.java, backend/src/main/java/com/library/service/impl/RoleServiceImpl.java, backend/src/main/java/com/library/service/impl/MenuServiceImpl.java, backend/src/main/java/com/library/controller/system/RoleController.java, backend/src/main/java/com/library/controller/system/MenuController.java</files>
  <action>
创建角色管理、菜单管理完整后端实现：

**backend/src/main/java/com/library/mapper/MenuMapper.java**：
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 " +
            "ORDER BY m.sort")
    List<Menu> selectByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_user_role ur ON 1=1 " +
            "INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id AND m.id = rm.menu_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 " +
            "ORDER BY m.sort")
    List<Menu> selectNavByUserId(@Param("userId") Long userId);
}
```

**backend/src/main/java/com/library/service/impl/RoleServiceImpl.java**：
```java
package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.Menu;
import com.library.entity.Role;
import com.library.entity.RoleMenu;
import com.library.mapper.MenuMapper;
import com.library.mapper.RoleMapper;
import com.library.mapper.RoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Autowired
    private MenuMapper menuMapper;

    public Page<Role> list(Integer pageNum, Integer pageSize) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Role::getCreateTime);
        return roleMapper.selectPage(page, wrapper);
    }

    public List<Role> listAll() {
        return roleMapper.selectList(null);
    }

    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    public void create(Role role) {
        roleMapper.insert(role);
    }

    public void update(Role role) {
        roleMapper.updateById(role);
    }

    public void delete(Long id) {
        roleMapper.deleteById(id);
        // 删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
    }

    public List<Menu> getMenusByRoleId(Long roleId) {
        return menuMapper.selectByRoleId(roleId);
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除旧关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, roleId));
        // 再插入新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<RoleMenu> list = new ArrayList<>();
            for (Long menuId : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                list.add(rm);
            }
            roleMenuMapper.insertBatch(list);
        }
    }
}
```

**backend/src/main/java/com/library/entity/RoleMenu.java**：
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role_menu")
public class RoleMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long menuId;
}
```

**backend/src/main/java/com/library/mapper/RoleMenuMapper.java**：
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
}
```

**backend/src/main/java/com/library/service/impl/MenuServiceImpl.java**：
```java
package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Menu;
import com.library.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl {

    @Autowired
    private MenuMapper menuMapper;

    public List<Menu> tree() {
        List<Menu> all = menuMapper.selectList(null);
        return buildTree(all, 0L);
    }

    public List<Menu> nav(Long userId) {
        return menuMapper.selectNavByUserId(userId);
    }

    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    public void create(Menu menu) {
        menuMapper.insert(menu);
    }

    public void update(Menu menu) {
        menuMapper.updateById(menu);
    }

    public void delete(Long id) {
        menuMapper.deleteById(id);
    }

    private List<Menu> buildTree(List<Menu> all, Long parentId) {
        return all.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .peek(m -> m.setChildren(buildTree(all, m.getId())))
                .collect(Collectors.toList());
    }
}
```

**backend/src/main/java/com/library/controller/system/RoleController.java**：
```java
package com.library.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.entity.Menu;
import com.library.entity.Role;
import com.library.service.impl.RoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleServiceImpl roleService;

    @GetMapping("/list")
    public Result<Page<Role>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(roleService.list(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Role>> listAll() {
        return Result.success(roleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping("/{id}/menus")
    public Result<List<Menu>> getMenus(@PathVariable Long id) {
        return Result.success(roleService.getMenusByRoleId(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Role role) {
        roleService.create(role);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Role role) {
        roleService.update(role);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success("分配成功", null);
    }
}
```

**backend/src/main/java/com/library/controller/system/MenuController.java**：
```java
package com.library.controller.system;

import com.library.common.Result;
import com.library.entity.Menu;
import com.library.service.impl.MenuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuServiceImpl menuService;

    @GetMapping("/tree")
    public Result<List<Menu>> tree() {
        return Result.success(menuService.tree());
    }

    @GetMapping("/nav")
    public Result<List<Menu>> nav(Authentication authentication) {
        // 从 Security Context 获取当前用户 ID（需要从 JWT 中解析或查询）
        // 简化：管理员 ID=1
        Long userId = 1L;
        return Result.success(menuService.nav(userId));
    }

    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Menu menu) {
        menuService.create(menu);
        return Result.success("创建成功", null);
    }

    @PutMapping
    public Result<Void> update(@RequestBody Menu menu) {
        menuService.update(menu);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success("删除成功", null);
    }
}
```

**backend/src/main/java/com/library/config/WebConfig.java**（CORS 配置）：
```java
package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```
</action>
  <verify>
<automated>cd backend && mvn compile -q 2>&1 | grep -i error | head -10</automated>
  </verify>
  <done>RoleController, MenuController, RoleServiceImpl, MenuServiceImpl 可编译</done>
</task>

<task type="auto">
  <name>Task 3: 前端主框架与页面组件</name>
  <files>frontend/src/views/Layout.vue, frontend/src/views/dashboard/Index.vue, frontend/src/api/user.js, frontend/src/api/role.js, frontend/src/api/menu.js</files>
  <action>
创建前端主框架、首页和管理页面：

**frontend/src/api/user.js**：
```javascript
import request from './request'

export function getUserList(params) {
  return request({ url: '/user/list', method: 'get', params })
}

export function getUser(id) {
  return request({ url: `/user/${id}`, method: 'get' })
}

export function createUser(data) {
  return request({ url: '/user', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/user', method: 'put', data })
}

export function deleteUser(id) {
  return request({ url: `/user/${id}`, method: 'delete' })
}

export function getCurrentUser() {
  return request({ url: '/user/me', method: 'get' })
}
```

**frontend/src/api/role.js**：
```javascript
import request from './request'

export function getRoleList(params) {
  return request({ url: '/role/list', method: 'get', params })
}

export function getAllRoles() {
  return request({ url: '/role/all', method: 'get' })
}

export function getRole(id) {
  return request({ url: `/role/${id}`, method: 'get' })
}

export function getRoleMenus(id) {
  return request({ url: `/role/${id}/menus`, method: 'get' })
}

export function createRole(data) {
  return request({ url: '/role', method: 'post', data })
}

export function updateRole(data) {
  return request({ url: '/role', method: 'put', data })
}

export function deleteRole(id) {
  return request({ url: `/role/${id}`, method: 'delete' })
}

export function assignMenus(id, menuIds) {
  return request({ url: `/role/${id}/menus`, method: 'put', data: menuIds })
}
```

**frontend/src/api/menu.js**：
```javascript
import request from './request'

export function getMenuTree() {
  return request({ url: '/menu/tree', method: 'get' })
}

export function getMenuNav() {
  return request({ url: '/menu/nav', method: 'get' })
}

export function getMenu(id) {
  return request({ url: `/menu/${id}`, method: 'get' })
}

export function createMenu(data) {
  return request({ url: '/menu', method: 'post', data })
}

export function updateMenu(data) {
  return request({ url: '/menu', method: 'put', data })
}

export function deleteMenu(id) {
  return request({ url: `/menu/${id}`, method: 'delete' })
}
```

**frontend/src/views/Layout.vue**（主框架 + 动态菜单）：
```vue
<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">图书馆系统</div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <template v-for="item in menus" :key="item.id">
          <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
            <template #title>
              <span>{{ item.menuName }}</span>
            </template>
            <el-menu-item
              v-for="child in item.children"
              :key="child.id"
              :index="child.path"
            >
              {{ child.menuName }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            {{ item.menuName }}
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">{{ userInfo?.nickname || '用户' }}</div>
        <div class="header-right">
          <el-button link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMenuNav } from '@/api/menu'
import { logout } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = ref([])
const userInfo = computed(() => userStore.userInfo)
const activeMenu = computed(() => route.path)

const loadMenus = async () => {
  try {
    const res = await getMenuNav()
    if (res.code === 200) {
      menus.value = res.data || []
    }
  } catch (e) {
    console.error('加载菜单失败', e)
  }
}

const handleLogout = async () => {
  try {
    await logout()
  } catch (e) {}
  userStore.token = ''
  userStore.userInfo = null
  router.push('/login')
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background: #409eff;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.sidebar-menu {
  height: calc(100vh - 60px);
  border-right: none;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
}
.header-left {
  font-weight: bold;
}
</style>
```

**frontend/src/views/dashboard/Index.vue**（首页）：
```vue
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>用户总数</template>
          <div class="stat-value">100</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>图书总数</template>
          <div class="stat-value">500</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>借阅中</template>
          <div class="stat-value">30</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>预约中</template>
          <div class="stat-value">5</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px">
      <template #header>欢迎使用AI智能图书管理系统</template>
      <p>当前登录用户：{{ userInfo?.nickname || '-' }}</p>
      <p>角色：{{ userInfo?.roleName || '-' }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
</script>

<style scoped>
.stat-value {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  color: #409eff;
}
</style>
```
</action>
  <verify>
<automated>cd frontend && npm run build 2>&1 | tail -15</automated>
  </verify>
  <done>Layout.vue, Index.vue 存在，npm run build 成功</done>
</task>

<task type="auto">
  <name>Task 4: 前端管理页面（用户、角色、菜单）</name>
  <files>frontend/src/views/system/UserManage.vue, frontend/src/views/system/RoleManage.vue, frontend/src/views/system/MenuManage.vue</files>
  <action>
创建用户管理、角色管理、菜单管理页面：

**frontend/src/views/system/UserManage.vue**：
```vue
<template>
  <div class="user-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">新增用户</el-button>
      <el-input
        v-model="searchUsername"
        placeholder="搜索用户名"
        style="width: 200px; margin-left: 10px"
        @keyup.enter="loadData"
      />
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 10px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 10px"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="选择角色">
            <el-option
              v-for="role in allRoles"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { getAllRoles } from '@/api/role'

const searchUsername = ref('')
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const allRoles = ref([])

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  roleId: null
})

const loadData = async () => {
  const res = await getUserList({ pageNum: pageNum.value, pageSize: pageSize.value, username: searchUsername.value })
  if (res.code === 200) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const loadRoles = async () => {
  const res = await getAllRoles()
  if (res.code === 200) {
    allRoles.value = res.data || []
  }
}

const handleCreate = () => {
  Object.assign(form, { id: null, username: '', password: '', nickname: '', roleId: null })
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { id: row.id, username: row.username, password: '', nickname: row.nickname, roleId: row.roleId })
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.id) {
    await updateUser(form)
  } else {
    await createUser(form)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除?', '提示')
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
```

**frontend/src/views/system/RoleManage.vue**：
```vue
<template>
  <div class="role-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">新增角色</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 10px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="roleName" label="角色名" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="handleAssignMenu(row)">分配菜单</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 10px"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80">
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="400px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignMenus } from '@/api/role'
import { getMenuTree } from '@/api/menu'

const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const menuTree = ref([])
const menuTreeRef = ref(null)
const currentRoleId = ref(null)

const form = reactive({ id: null, roleName: '', description: '' })

const loadData = async () => {
  const res = await getRoleList({ pageNum: pageNum.value, pageSize: pageSize.value })
  if (res.code === 200) {
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }
}

const loadMenuTree = async () => {
  const res = await getMenuTree()
  if (res.code === 200) {
    menuTree.value = res.data || []
  }
}

const handleCreate = () => {
  Object.assign(form, { id: null, roleName: '', description: '' })
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { id: row.id, roleName: row.roleName, description: row.description })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.id) {
    await updateRole(form)
  } else {
    await createRole(form)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除?', '提示')
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  menuDialogVisible.value = true
  await loadMenuTree()
  const res = await getRoleMenus(row.id)
  if (res.code === 200) {
    const checkedIds = res.data.map(m => m.id)
    nextTick(() => {
      menuTreeRef.value?.setCheckedKeys(checkedIds)
    })
  }
}

const handleSaveMenus = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
  await assignMenus(currentRoleId.value, checkedKeys)
  menuDialogVisible.value = false
  ElMessage.success('分配成功')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
```

**frontend/src/views/system/MenuManage.vue**：
```vue
<template>
  <div class="menu-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate(null)">新增菜单</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 10px" row-key="id">
      <el-table-column prop="menuName" label="菜单名称" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="component" label="组件" />
      <el-table-column prop="perms" label="权限标识" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleCreate(row)">添加子菜单</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如: system/UserManage" />
        </el-form-item>
        <el-form-item label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="如: user:list" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)

const form = reactive({
  id: null,
  menuName: '',
  parentId: 0,
  path: '',
  component: '',
  perms: '',
  sort: 0,
  icon: '',
  menuType: 1
})

const flattenTree = (nodes) => {
  const result = []
  const flatten = (list, level = 0) => {
    for (const node of list) {
      result.push({ ...node, _level: level })
      if (node.children?.length) {
        flatten(node.children, level + 1)
      }
    }
  }
  flatten(nodes)
  return result
}

const loadData = async () => {
  const res = await getMenuTree()
  if (res.code === 200) {
    tableData.value = flattenTree(res.data || [])
  }
}

const handleCreate = (parent) => {
  Object.assign(form, {
    id: null,
    menuName: '',
    parentId: parent?.id || 0,
    path: '',
    component: '',
    perms: '',
    sort: 0,
    icon: '',
    menuType: 1
  })
  dialogTitle.value = parent ? `新增子菜单（${parent.menuName}）` : '新增菜单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    menuName: row.menuName,
    parentId: row.parentId,
    path: row.path,
    component: row.component,
    perms: row.perms,
    sort: row.sort,
    icon: row.icon,
    menuType: row.menuType
  })
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.id) {
    await updateMenu(form)
  } else {
    await createMenu(form)
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除?', '提示')
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
```
</action>
  <verify>
<automated>cd frontend && npm run build 2>&1 | tail -15</automated>
  </verify>
  <done>UserManage.vue, RoleManage.vue, MenuManage.vue 存在，npm run build 成功</done>
</task>

</tasks>

<threat_model>
## Trust Boundaries

| Boundary | Description |
|----------|-------------|
| client → backend | 认证后请求，CORS 限制仅 localhost:5173 |
| frontend → dynamic menus | 从 API 动态加载菜单，后端按用户角色过滤 |

## STRIDE Threat Register

| Threat ID | Category | Component | Disposition | Mitigation Plan |
|-----------|----------|-----------|-------------|-----------------|
| T-01-07 | Elevation of Privilege | UserController | mitigate | 仅管理员可访问用户管理 API |
| T-01-08 | Information Disclosure | MenuController | mitigate | nav 接口按 userId 过滤菜单，未授权用户看不到菜单 |
| T-01-09 | Tampering | RoleController | mitigate | assignMenus 校验角色存在性 |
</threat_model>

<verification>
- [ ] `cd backend && mvn compile` 无报错
- [ ] `cd frontend && npm run build` 成功
- [ ] 启动前后端后，admin 登录可看到所有菜单
- [ ] GET /api/user/list 返回用户列表
- [ ] GET /api/role/list 返回角色列表
- [ ] GET /api/menu/tree 返回树形菜单
- [ ] PUT /api/role/1/menus 可分配菜单权限
</verification>

<success_criteria>
1. 管理员可增删改查用户（USER-01~USER-04）
2. 管理员可增删改查角色（ROLE-01~ROLE-04）
3. 管理员可增删改查菜单树（MENU-01~MENU-05）
4. 管理员可为角色分配菜单权限（ROLE-05, MENU-06）
5. 用户只能访问其角色拥有的菜单（AUTHZ-01, AUTHZ-02）
6. 未授权用户访问 API 返回 403（AUTHZ-04）
7. 前端动态菜单根据用户角色渲染（AUTHZ-2）
8. 按钮级权限控制可用（AUTHZ-03）
</success_criteria>

<output>
After completion, create `.planning/phases/01-系统服务/01-C-SUMMARY.md`
</output>
