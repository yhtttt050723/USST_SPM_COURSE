# 数据库简要设计（MySQL 8.0 / utf8mb4 / InnoDB）

> 对应《项目计划.md》及最新需求文档，仅保留课程实践需要的核心表与字段。所有表默认包含 `id (bigint, PK)`、`created_at`、`updated_at`、`deleted`。

## 1. 用户与角色
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `users` | `student_no`、`email`、`name`、`password`、`avatar_url`、`status` | 账号主体，状态：1 启用 / 0 禁用 |
| `roles` | `code`（STUDENT/TEACHER/ADMIN） | 角色字典 |
| `user_roles` | `user_id`、`role_id` | 多对多授权 |

## 2. 课程与班级
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `courses` | `code`、`name`、`semester`、`description` | 课程信息 |
| `class_sections` | `course_id`、`name`、`teacher_id`、`schedule` | 班级 |
| `enrollments` | `class_id`、`student_id`、`status` | 学生加入班级 |
| `announcements` | `class_id`、`title`、`content`、`visible_from/to` | 班级公告（迭代 2） |

## 3. 作业与成绩
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `assignments` | `class_id`、`title`、`description`、`type`、`total_score`、`due_at`、`allow_resubmit` | 作业 |
| `assignment_files` | `assignment_id`、`file_id` | 作业附件 |
| `submissions` | `assignment_id`、`student_id`、`content`、`status`（SUBMITTED/DRAFT） | 提交记录 |
| `submission_files` | `submission_id`、`file_id` | 提交附件 |
| `grades` | `submission_id`、`scorer_id`、`score`、`feedback`、`released` | 评分 |

索引建议：`submissions (assignment_id, student_id)`、`grades (submission_id)`、`assignments (class_id, due_at)`。

## 4. 出勤
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `attendance_sessions` | `class_id`、`title`、`mode`（CODE/GEO）`start_at`、`end_at`、`code` | 考勤任务 |
| `attendance_records` | `session_id`、`student_id`、`status`（PRESENT/LATE/ABSENT）`checkin_at`、`checkout_at`、`location` | 学生签到 |
| `leave_requests` | `session_id`、`student_id`、`reason`、`attachment_id`、`status` | 请假审批 |

## 5. 讨论与通知（迭代 2）
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `discussions` | `class_id`、`author_id`、`title`、`content`、`pin` | 讨论帖 |
| `comments` | `discussion_id`、`parent_id`、`author_id`、`content` | 评论（支持二级） |
| `notifications` | `event_type`、`payload` | 事件记录（作业提醒、公告等） |
| `user_notifications` | `notification_id`、`user_id`、`status`（UNREAD/READ）`read_at` | 用户通知状态 |

## 6. 文件与公共表
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `files` | `file_name`、`storage_path`、`mime_type`、`file_size`、`uploader_id` | 文件元数据（作业、附件通用） |

## 7. 视图/统计示例
- `vw_student_progress`：汇总学生在某班级的作业得分、提交率、出勤率。  
- `vw_class_attendance`：按考勤任务统计出勤、迟到、缺席。  
> 视图具体 SQL 可根据需要在演示前生成。

## 8. 版本与迁移
- 使用 Flyway/Liquibase 管理脚本，例如 `V20241117__init_core_tables.sql`。  
- 迭代 1 提交：用户/课程/班级/作业/出勤/文件等核心表。  
- 迭代 2 追加讨论、公告、通知等表。  

如需扩展（刷题、考试等），可复用 `assignments/submissions` 结构，新建模块化表即可。***

