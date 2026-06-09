# Phase 1: 系统服务 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-09
**Phase:** 1-系统服务
**Areas discussed:** JWT 存储位置, 数据库访问层, 前端状态管理, 初始管理员账户

---

## JWT 存储位置

| Option | Description | Selected |
|--------|-------------|----------|
| localStorage | 简单，但 XSS 攻击风险 | |
| Cookie | 更安全，防 XSS，自动发送 | ✓ |
| HttpOnly Cookie | 更安全，防 CSRF，需要额外处理 | |

**User's choice:** Cookie
**Notes:** 平衡安全性和易用性，CookieHttpOnly 是标准做法

---

## 数据库访问层

| Option | Description | Selected |
|--------|-------------|----------|
| MySQL 直接设计 | 传统关系型，课堂学过 | |
| MyBatis-Plus | ORM 简化开发，MySQL 通用 | ✓ |
| JPA/Hibernate | 全自动化 ORM，学习曲线 | |

**User's choice:** MyBatis-Plus
**Notes:** 推荐选择，开发效率高，课堂也熟悉

---

## 前端状态管理

| Option | Description | Selected |
|--------|-------------|----------|
| Pinia | Vue 3 官方方案，轻量 | ✓ |
| Vuex | Vue 2 主流方案，文档多 | |
| 简单场景够用 | 不用状态管理，直接 prop | |

**User's choice:** Pinia
**Notes:** Vue 3 官方推荐，学习价值高

---

## 初始管理员账户

| Option | Description | Selected |
|--------|-------------|----------|
| 需要默认管理员账户 | 预设 admin/admin123 | ✓ |
| 不需要，初始化空数据库 | 手动创建第一个管理员 | |

**User's choice:** 需要默认管理员账户
**Notes:** 演示方便，作业需要可运行的系统

---

## Claude's Discretion

- 前端目录结构、组件拆分由规划阶段决定
- Nacos 配置中心的具体 namespace/group 设置由规划阶段决定

## Deferred Ideas

无

