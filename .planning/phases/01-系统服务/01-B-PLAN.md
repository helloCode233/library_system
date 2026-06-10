---
phase: "01-系统服务"
plan: "B"
type: "execute"
wave: 1
depends_on: []
files_modified:
  - "backend/src/main/java/com/library/entity/User.java"
  - "backend/src/main/java/com/library/entity/Role.java"
  - "backend/src/main/java/com/library/entity/Menu.java"
  - "backend/src/main/java/com/library/mapper/UserMapper.java"
  - "backend/src/main/java/com/library/service/impl/AuthServiceImpl.java"
  - "backend/src/main/java/com/library/controller/system/AuthController.java"
  - "backend/src/main/java/com/library/security/JwtTokenProvider.java"
  - "backend/src/main/java/com/library/security/JwtAuthenticationFilter.java"
  - "backend/src/main/java/com/library/security/UserDetailsServiceImpl.java"
  - "backend/src/main/java/com/library/config/SecurityConfig.java"
  - "backend/src/main/java/com/library/dto/LoginRequest.java"
  - "backend/src/main/java/com/library/dto/LoginResponse.java"
  - "backend/src/main/java/com/library/common/Result.java"
  - "frontend/src/views/Login.vue"
  - "frontend/src/api/auth.js"
autonomous: true
requirements:
  - "AUTH-01"
  - "AUTH-02"
  - "AUTH-03"
  - "AUTH-04"
  - "AUTH-05"
must_haves:
  truths:
    - "用户可以使用用户名密码注册账号"
    - "用户可以使用用户名密码登录并获得 JWT Token"
    - "JWT Token 存储在 HttpOnly Cookie 中"
    - "前端请求携带 Cookie 访问受保护资源"
  artifacts:
    - path: "backend/src/main/java/com/library/controller/system/AuthController.java"
      provides: "注册、登录、登出 API 端点"
      exports: "POST /api/auth/register, POST /api/auth/login, POST /api/auth/logout"
    - path: "backend/src/main/java/com/library/security/JwtTokenProvider.java"
      provides: "JWT Token 生成与校验"
    - path: "backend/src/main/java/com/library/config/SecurityConfig.java"
      provides: "Spring Security 配置（JWT 过滤器链）"
    - path: "frontend/src/views/Login.vue"
      provides: "登录页面 UI"
    - path: "frontend/src/api/auth.js"
      provides: "登录相关 API 请求封装"
  key_links:
    - from: "Login.vue"
      to: "AuthController /api/auth/login"
      via: "axios POST，Cookie 存储 token"
    - from: "JwtAuthenticationFilter"
      to: "SecurityConfig"
      via: "Filter 注入 Security Filter Chain"
    - from: "JwtTokenProvider"
      to: "UserDetailsServiceImpl"
      via: "从数据库加载用户信息校验密码"
---

<objective>
实现用户认证核心功能：注册、登录、JWT Token 生成与校验、Cookie 存储、Spring Security 过滤器链。
</objective>

<execution_context>
@$HOME/.claude/get-shit-done/workflows/execute-plan.md
@$HOME/.claude/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/phases/01-系统服务/SKELETON.md
@.planning/phases/01-系统服务/01-A-PLAN.md
</context>

<interfaces>
From backend/src/main/resources/application.yml:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library
    username: root
    password: root
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

From sql/init.sql:
```sql
-- admin / admin123 BCrypt password hash
INSERT INTO sys_user (username, password, nickname, role_id) VALUES
  ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1);
```
</interfaces>

<tasks>

<task type="auto">
  <name>Task 1: 后端实体类与通用响应</name>
  <files>backend/src/main/java/com/library/entity/User.java, backend/src/main/java/com/library/entity/Role.java, backend/src/main/java/com/library/entity/Menu.java, backend/src/main/java/com/library/common/Result.java</files>
  <action>
创建后端实体类和通用响应封装：

**backend/src/main/java/com/library/entity/User.java**：
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private Long roleId;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**backend/src/main/java/com/library/entity/Role.java**：
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String description;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**backend/src/main/java/com/library/entity/Menu.java**：
```java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String menuName;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private String perms;
    private Integer menuType;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**backend/src/main/java/com/library/common/Result.java**：
```java
package com.library.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
```
</action>
  <verify>
<automated>cd backend && mvn compile -q 2>&1 | grep -i error | head -10</automated>
  </verify>
  <done>User.java, Role.java, Menu.java, Result.java 存在且可编译</done>
</task>

<task type="auto">
  <name>Task 2: JWT Token 提供者与认证过滤器</name>
  <files>backend/src/main/java/com/library/security/JwtTokenProvider.java, backend/src/main/java/com/library/security/JwtAuthenticationFilter.java, backend/src/main/java/com/library/security/UserDetailsServiceImpl.java, backend/src/main/java/com/library/mapper/UserMapper.java</files>
  <action>
创建 JWT 认证核心组件：

**backend/src/main/java/com/library/mapper/UserMapper.java**：
```java
package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

**backend/src/main/java/com/library/security/JwtTokenProvider.java**：
```java
package com.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:library-secret-key-for-jwt-token-generation-must-be-long-enough}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // 24小时

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**backend/src/main/java/com/library/security/UserDetailsServiceImpl.java**：
```java
package com.library.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
```

**backend/src/main/java/com/library/security/JwtAuthenticationFilter.java**：
```java
package com.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // 优先从 Cookie 获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 备用：从 Header 获取（Authorization: Bearer xxx）
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```
</action>
  <verify>
<automated>cd backend && mvn compile -q 2>&1 | grep -i error | head -10</automated>
  </verify>
  <done>JwtTokenProvider.java, JwtAuthenticationFilter.java, UserDetailsServiceImpl.java 可编译</done>
</task>

<task type="auto">
  <name>Task 3: Spring Security 配置与认证服务</name>
  <files>backend/src/main/java/com/library/config/SecurityConfig.java, backend/src/main/java/com/library/service/impl/AuthServiceImpl.java, backend/src/main/java/com/library/controller/system/AuthController.java, backend/src/main/java/com/library/dto/LoginRequest.java, backend/src/main/java/com/library/dto/LoginResponse.java</files>
  <action>
创建 Security 配置、认证服务和 Controller：

**backend/src/main/java/com/library/dto/LoginRequest.java**：
```java
package com.library.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

**backend/src/main/java/com/library/dto/LoginResponse.java**：
```java
package com.library.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String roleName;
    }
}
```

**backend/src/main/java/com/library/service/impl/AuthServiceImpl.java**：
```java
package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roleName("管理员") // TODO: 从角色表查询
                .build();

        return LoginResponse.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

    public void register(User user) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    public User getCurrentUser(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }
}
```

**backend/src/main/java/com/library/config/SecurityConfig.java**：
```java
package com.library.config;

import com.library.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**backend/src/main/java/com/library/controller/system/AuthController.java**：
```java
package com.library.controller.system;

import com.library.common.Result;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.User;
import com.library.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        // Token 存入 Cookie（HttpOnly）
        Cookie cookie = new Cookie("token", loginResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24小时
        response.addCookie(cookie);

        return Result.success(loginResponse);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        authService.register(user);
        return Result.success("注册成功", null);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return Result.success("登出成功", null);
    }
}
```
</action>
  <verify>
<automated>cd backend && mvn compile -q 2>&1 | grep -i error | head -10</automated>
  </verify>
  <done>SecurityConfig.java, AuthServiceImpl.java, AuthController.java 可编译</done>
</task>

<task type="auto">
  <name>Task 4: 前端登录页面与 API</name>
  <files>frontend/src/views/Login.vue, frontend/src/api/auth.js</files>
  <action>
创建前端登录页面和 API 封装：

**frontend/src/api/auth.js**：
```javascript
import request from './request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
```

**frontend/src/views/Login.vue**：
```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>AI智能图书管理系统</span>
        </div>
      </template>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="tips">
        默认账户：admin / admin123
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(loginForm)
    if (res.code === 200) {
      userStore.token = res.data.token
      userStore.userInfo = res.data.user
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}
.tips {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-top: 10px;
}
</style>
```
</action>
  <verify>
<automated>cd frontend && npm run build 2>&1 | tail -15</automated>
  </verify>
  <done>Login.vue 和 auth.js 存在，npm run build 成功</done>
</task>

</tasks>

<threat_model>
## Trust Boundaries

| Boundary | Description |
|----------|-------------|
| client → backend | JWT Token 通过 HttpOnly Cookie 传递，XSS 无法读取 |
| backend → DB | 用户密码 BCrypt 加密后比对，无法反向解密 |

## STRIDE Threat Register

| Threat ID | Category | Component | Disposition | Mitigation Plan |
|-----------|----------|-----------|-------------|-----------------|
| T-01-04 | Information Disclosure | JwtTokenProvider | mitigate | JWT secret 至少 256bit，存储在配置中 |
| T-01-05 | Tampering | JWT Token | mitigate | Token 签名验证，失效则拒绝请求 |
| T-01-06 | Elevation of Privilege | AuthController | mitigate | Spring Security 配置 .authenticated()，未登录返回 401 |
</threat_model>

<verification>
- [ ] `cd backend && mvn compile` 无报错
- [ ] `cd frontend && npm run build` 成功
- [ ] 启动后端后，POST /api/auth/login 返回 token
- [ ] 登录成功后 Cookie 中存在 token
</verification>

<success_criteria>
1. POST /api/auth/register 可注册新用户
2. POST /api/auth/login (admin/admin123) 返回 JWT Token 并设置 Cookie
3. GET /api/auth/me 可获取当前登录用户信息（携带 Cookie）
4. POST /api/auth/logout 可清除 Cookie
5. 前端登录页可正常显示并完成登录流程
</success_criteria>

<output>
After completion, create `.planning/phases/01-系统服务/01-B-SUMMARY.md`
</output>
