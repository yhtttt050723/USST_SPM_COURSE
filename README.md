# USST SPM 课程平台

一套面向《项目管理与过程改进》课程的教学管理系统，覆盖课堂中“课程-作业-出勤-讨论-统计”全流程，支撑课程设计与最终答辩演示。

## 技术栈
- 前端：Vue 3 + Vite + Element Plus  
- 后端：Spring Boot 3.5.6 + MyBatis Plus  
- 数据库：MySQL 8.0  
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

## 本仓库包含
- 迭代 1：登录、课程/作业/出勤基础流程（最小演示版）  
- 迭代 2：教师后台、讨论区、公告、统计与文档交付（规划中）  
- 文档与代码保持同步，可直接用于课程汇报或答辩演示。

## 如何使用
1. 安装环境：Node.js 20+、Java 17+、MySQL 8+。  
2. 导入 `SqldDesign.md` 中的核心表或根据 SQL 初始化。  
3. 后端：`cd Backend/demo && ./mvnw spring-boot:run`。  
4. 前端：`cd frontend && npm install && npm run dev`。  
5. 使用 `API.json` 导入 Swagger/ApiFox，辅助联调与演示。

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

