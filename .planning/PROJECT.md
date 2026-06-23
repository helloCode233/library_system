# AI智能图书管理系统

## What This Is

基于 Spring Cloud + Nacos 的微服务架构AI智能图书管理系统，包含用户认证、RBAC 动态权限管理和图书馆核心业务（图书管理、借阅管理）。提供 Vue 前端界面，课程作业演示用 MVP。

## Core Value

完成课程作业要求：微服务架构（Nacos 服务注册与发现）+ 完整可运行的用户权限系统 + 图书馆业务功能。

## Requirements

### Active

- [ ] 用户注册、登录、登出（JWT Token）
- [ ] 用户管理（CRUD）
- [ ] 角色管理（CRUD）
- [ ] 菜单管理（树形结构、动态配置）
- [ ] 动态菜单（根据用户角色渲染侧边栏）
- [ ] 按钮级别权限控制
- [ ] 图书管理（CRUD、分类、搜索）
- [ ] 借阅管理（借书、还书、借阅记录）
- [ ] 管理员和普通用户两种角色

### Out of Scope

- 超期罚款计算
- 邮件/短信通知
- OAuth 第三方登录
- 分布式事务（课程要求单体部署）
- 微服务间熔断、限流（课程要求单体部署）

## Context

**技术栈：**
- 后端：Spring Cloud (Spring Boot) + Nacos（服务注册与发现）+ MySQL
- 前端：Vue.js + Element UI
- 网关：Nacos Gateway

**微服务规划：**
- system-service：用户、角色、菜单、权限
- library-service：图书、借阅
- gateway：统一入口路由

**部署方式：** 单体部署（业务拆分为 3 个服务，但打包为单个应用实例），Nacos 仅用于服务注册与发现。

**MVP 目标：** 跑起来能演示，界面可用，后端接口完整。

## Constraints

- **微服务数量**：最多 3 个服务
- **部署方式**：单体部署（演示用）
- **技术栈**：Spring Cloud + Nacos + MySQL + Vue
- **时间**：课程作业周期

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 单体部署 | 课程要求简化运维，聚焦微服务概念 | ✓ MVP 合适 |
| JWT 认证 | 无状态会话，适合前后端分离 | ✓ 简单够用 |
| 动态菜单 | RBAC 核心特性，展示权限系统完整性 | ✓ 必需 |

---
*Last updated: 2026/05/09 after initialization*
