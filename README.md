# Course-Design-of-Project-Process-and-Project-Management
Course Design of Project Process and Project Management
## 项目概述

本项目是为上海理工大学项目管理与过程改进课程开发的在线教学管理平台，提供完整的课程管理、学习资源、作业提交、出勤管理等功能。

## 技术架构

- **前端**: Vue 3 + Vite + Element Plus
- **后端**: Spring Boot 3.5.6 + MyBatis Plus
- **数据库**: MySQL 8.0
- **部署**: Docker + Nginx

## 项目文档结构

### 1. 需求文档.md
**文件位置**: `RequirementDocument.md`

**内容概述**:
- 项目背景和目标
- 功能需求详细说明
- 非功能需求
- 用户角色定义
- 界面需求
- 系统集成需求
- 部署需求
- 测试需求
- 维护需求

**主要模块**:
- 用户管理模块
- 课程管理模块
- 题目管理模块
- 刷题系统模块
- 作业管理模块
- 出勤管理模块
- 成绩管理模块
- 消息通知模块

### 2. API接口文档.md
**文件位置**: `APIDocument.md`

**内容概述**:
- 接口基础信息
- 通用响应格式
- 状态码说明
- 各模块API接口详细说明
- 错误处理
- 接口测试说明

**主要接口模块**:
- 用户认证接口
- 用户管理接口
- 课程管理接口
- 题目管理接口
- 刷题系统接口
- 作业管理接口
- 出勤管理接口
- 成绩管理接口
- 消息通知接口
- 文件管理接口
- 统计分析接口
- 系统管理接口

### 3. 可导入软件的API.json
**文件位置**: `API.json`

**内容概述**:
- 完整的OpenAPI 3.0格式API文档
- 可直接导入API管理工具
- 包含所有接口的详细定义
- 包含请求/响应示例
- 包含数据模型定义


### 4. 数据库设计文档.md
**文件位置**: `SqlDesign.md`

**内容概述**:
- 数据库概述和设计原则
- 完整的表结构设计
- 索引设计
- 数据库初始化脚本
- 数据优化建议
- 数据迁移脚本
- 数据库监控

**主要数据表**:
- 用户管理相关表（users, user_sessions）
- 课程管理相关表（courses, course_announcements, course_students）
- 题目管理相关表（questions, question_categories, question_tags）
- 刷题系统相关表（practice_sessions, practice_questions, wrong_questions）
- 作业管理相关表（assignments, assignment_submissions, assignment_answers）
- 出勤管理相关表（attendance_records, leave_requests）
- 成绩管理相关表（grades）
- 消息通知相关表（messages, message_attachments）
- 文件管理相关表（files）
- 系统管理相关表（system_configs, operation_logs）

## 项目特色功能

### 1. 智能刷题系统
- 支持按分类、难度练习
- 错题本功能
- 学习进度跟踪
- 知识点掌握情况分析

### 2. 完整的作业管理
- 在线作业发布
- 自动批改（客观题）
- 手动批改（主观题）
- 作业统计分析

### 3. 出勤管理系统
- 签到签退功能
- 请假申请流程
- 出勤率统计
- 异常处理

### 4. 成绩管理
- 多维度成绩录入
- 成绩统计分析
- 成绩排名
- 成绩趋势分析

### 5. 消息通知系统
- 实时消息推送
- 多种消息类型
- 消息状态管理
- 广播消息功能

