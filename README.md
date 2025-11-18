# USST SPM 课程平台

一套面向《项目管理与过程改进》课程的教学管理系统，覆盖课堂中“课程-作业-出勤-讨论-统计”全流程，支撑课程设计与最终答辩演示。

**场景说明**：单老师、单班级，对接学校教务处SSO登录（学号+密码）。

## 技术栈
- 前端：Vue 3 + Vite + Element Plus  
- 后端：Spring Boot 3.5.6 + MyBatis Plus  
- 数据库：MySQL 8.0  
- 登录：对接学校SSO（`https://ids6.usst.edu.cn/authserver/login`）
- 部署：Docker + Nginx（可选本地运行）

## 快速了解
| 文件 | 说明 |
| --- | --- |
| `项目计划.md` | 迭代目标与排期，当前开发主线 |
| `RequirementDocument.md` | 精简需求说明：角色、范围、功能模块与交付物 |
| `SqldDesign.md` | 主要数据表及关系示意，方便建库/演示 |
| `APIDpocument.md` | 文字版 API 概览，适合查模块清单与示例 |
| `API.json` | OpenAPI 3.0.3，可导入 Swagger/ApiFox 直接调试 |
| `Backend/demo` / `frontend` | Spring Boot & Vue 工程源码 |

## 核心特性
- **SSO登录**：对接学校教务处统一认证，使用学号和密码登录
- **单课程场景**：固定1位教师、1个班级，简化管理流程
- **功能模块**：作业发布/提交/批改、考勤签到、讨论区（迭代2）、统计报表
- **文档完整**：需求、数据库、API文档齐全，支持快速开发与演示

## 本仓库包含
- 迭代 1：SSO登录、课程/作业/出勤基础流程（最小演示版）  
- 迭代 2：教师后台、讨论区、公告、统计与文档交付（规划中）  
- 文档与代码保持同步，可直接用于课程汇报或答辩演示。

## 如何使用
1. 安装环境：Node.js 20+、Java 17+、MySQL 8+。  
2. 导入 `SqldDesign.md` 中的核心表或根据 SQL 初始化。  
3. 配置SSO回调地址：在 `application.properties` 中设置 `sso.callback.url`。  
4. 后端：`cd Backend/demo && ./mvnw spring-boot:run`。  
5. 前端：`cd frontend && npm install && npm run dev`。  
6. 使用 `API.json` 导入 Swagger/ApiFox，辅助联调与演示。

## SSO登录流程
1. 前端调用 `GET /api/v1/auth/sso/login` 获取SSO登录URL
2. 用户跳转到学校SSO页面，输入学号和密码
3. SSO验证成功后回调到 `GET /api/v1/auth/sso/callback?ticket=xxx`
4. 后端验证ticket，创建/更新本地用户，返回JWT token
5. 前端保存token，完成登录

## 目录导航
```
USST_SPM_COURSE/
├── Backend/            # Spring Boot 工程
├── frontend/           # Vue 3 工程
├── RequirementDocument.md
├── SqldDesign.md
├── APIDpocument.md
├── API.json
└── 项目计划.md
```

欢迎根据课程要求扩展功能或替换部署方案。***

