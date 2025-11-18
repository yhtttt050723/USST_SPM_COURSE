# 数据库简要设计（MySQL 8.0 / utf8mb4 / InnoDB）

> 单老师、单班级场景，简化表结构。所有表默认包含 `id (bigint, PK)`、`created_at`、`updated_at`、`deleted`。

## 1. 用户与角色
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `users` | `student_no`（学号，UQ）、`name`、`password`（本地备用）、`role`（STUDENT/TEACHER）、`avatar_url`、`status` | 用户表，通过SSO登录后同步创建/更新 |
| `user_sessions` | `user_id`、`token`、`refresh_token`、`expires_at` | JWT会话（可选，如用Redis可省略） |

**说明**：
- `student_no` 作为唯一标识，对接SSO后自动填充
- `role` 字段：默认STUDENT，教师通过配置的学号或手动设置
- `password` 字段保留但可不使用（SSO认证）

## 2. 课程（单课程场景）
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `course` | `name`、`code`、`semester`、`description`、`teacher_id`（FK users） | 固定单课程，teacher_id关联唯一教师 |
| `announcements` | `course_id`、`title`、`content`、`created_by`、`visible_from/to` | 课程公告（迭代2） |

**说明**：不需要班级表（class_sections），直接使用course表，所有学生默认属于该课程。

## 3. 作业与成绩
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `assignments` | `course_id`、`title`、`description`、`type`、`total_score`、`due_at`、`allow_resubmit` | 作业 |
| `assignment_files` | `assignment_id`、`file_id` | 作业附件 |
| `submissions` | `assignment_id`、`student_id`、`content`、`status`（SUBMITTED/DRAFT） | 提交记录 |
| `submission_files` | `submission_id`、`file_id` | 提交附件 |
| `grades` | `submission_id`、`scorer_id`（教师ID）、`score`、`feedback`、`released` | 评分 |

**索引**：`submissions (assignment_id, student_id)`、`grades (submission_id)`、`assignments (course_id, due_at)`。

## 4. 出勤
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `attendance_sessions` | `course_id`、`title`、`mode`（CODE/GEO）、`start_at`、`end_at`、`code` | 考勤任务 |
| `attendance_records` | `session_id`、`student_id`、`status`（PRESENT/LATE/ABSENT）、`checkin_at`、`checkout_at`、`location` | 学生签到 |
| `leave_requests` | `session_id`、`student_id`、`reason`、`attachment_id`、`status` | 请假审批 |

## 5. 讨论与通知（迭代2）
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `discussions` | `course_id`、`author_id`、`title`、`content`、`pin` | 讨论帖 |
| `comments` | `discussion_id`、`parent_id`、`author_id`、`content` | 评论（支持二级） |
| `notifications` | `event_type`、`payload` | 事件记录（作业提醒、公告等） |
| `user_notifications` | `notification_id`、`user_id`、`status`（UNREAD/READ）、`read_at` | 用户通知状态 |

## 6. 文件与公共表
| 表 | 关键字段 | 说明 |
| --- | --- | --- |
| `files` | `file_name`、`storage_path`、`mime_type`、`file_size`、`uploader_id` | 文件元数据（作业、附件通用） |

## 7. 视图/统计示例
- `vw_student_progress`：汇总学生在课程中的作业得分、提交率、出勤率
- `vw_course_attendance`：按考勤任务统计出勤、迟到、缺席
> 视图具体SQL可根据需要在演示前生成

## 8. 版本与迁移
- 使用 Flyway/Liquibase 管理脚本，例如 `V20241117__init_core_tables.sql`
- 迭代1提交：用户/课程/作业/出勤/文件等核心表
- 迭代2追加讨论、公告、通知等表

## 9. SSO用户同步逻辑
- 首次SSO登录：根据返回的学号查询users表，不存在则创建（role默认STUDENT）
- 教师识别：可通过配置表或固定学号判断，首次登录时设置role=TEACHER
- 后续登录：更新last_login_at，保持用户信息同步
