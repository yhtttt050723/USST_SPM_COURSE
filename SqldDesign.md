# 项目管理与过程改进课程网站数据库设计

> 版本：v1.0（与《项目计划.md》及《RequirementDocument.md》保持同步）  
> 数据库：MySQL 8.0，字符集 utf8mb4，默认 InnoDB。

## 1. 设计范围与目标
- 覆盖迭代 1、迭代 2 所有功能（用户、课程、作业、出勤、讨论、通知、统计）。
- 兼顾演示/教学场景与后续扩展（多班级、多人协作、权限拓展）。
- 所有业务表统一继承审计字段：`created_at`、`created_by`、`updated_at`、`updated_by`、`deleted`。

## 2. ER 模型概览
- **用户域**：`users`、`roles`、`user_roles`。
- **课程域**：`courses`、`class_sections`、`enrollments`。
- **教学过程域**：`assignments`、`assignment_files`、`submissions`、`submission_files`、`grades`。
- **考勤域**：`attendance_sessions`、`attendance_records`、`leave_requests`。
- **互动域**：`discussions`、`comments`、`announcements`。
- **通知域**：`notifications`、`user_notifications`。
- **公共**：`files`（对象存储映射）。

## 3. 表结构定义
### 3.1 用户与权限
| 表 | 说明 |
| --- | --- |
| `users` | 账号主体，含学号/工号、邮箱、基础资料。 |
| `roles` | 角色枚举（STUDENT、TEACHER、ADMIN）。 |
| `user_roles` | 多对多授权表。 |

**users**
| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| id | bigint | PK | 雪花/自增 |
| student_no | varchar(32) | UQ 可空 | 学号/工号 |
| email | varchar(128) | UQ | 登录邮箱 |
| password | varchar(255) | 非空 | Bcrypt |
| name | varchar(64) | 非空 | 显示名称 |
| avatar_url | varchar(255) | | 头像 |
| status | tinyint | 默认1 | 1 启用 0 禁用 |
| last_login_at | datetime | | 最近登录 |
| audit fields | | | 共用 |

### 3.2 课程与班级
**courses**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| code | varchar(32) UQ | 课程编号 |
| name | varchar(128) | 课程名 |
| semester | varchar(32) | 学期 |
| cover_url | varchar(255) | 封面 |
| description | text | 简介 |

**class_sections**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| course_id | bigint FK(courses) |
| name | varchar(64) | 班级名称 |
| teacher_id | bigint FK(users) | 主讲 |
| schedule | varchar(128) | 上课时间/地点 |

**enrollments**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| class_id | bigint FK(class_sections) |
| student_id | bigint FK(users) |
| status | tinyint | 在读/退课 |

### 3.3 作业与成绩
**assignments**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| class_id | bigint FK(class_sections) |
| title | varchar(128) |
| description | text |
| type | varchar(32) | HOMEWORK/QUIZ |
| total_score | int | 默认 100 |
| submission_type | varchar(32) | TEXT/FILE/BOTH |
| allow_resubmit | tinyint | 0/1 |
| due_at | datetime |
| published_at | datetime |

**assignment_files**
| id | bigint PK |
| assignment_id | bigint |
| file_id | bigint FK(files) |

**submissions**
| id | bigint PK |
| assignment_id | bigint |
| student_id | bigint |
| content | longtext | JSON/富文本 |
| status | varchar(32) | SUBMITTED/DRAFT |
| submitted_at | datetime |
| last_scored_at | datetime |

**submission_files**
| id | bigint PK |
| submission_id | bigint |
| file_id | bigint |

**grades**
| id | bigint PK |
| submission_id | bigint |
| scorer_id | bigint |
| score | int |
| feedback | text |
| released | tinyint |
| released_at | datetime |

### 3.4 出勤
**attendance_sessions**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| class_id | bigint |
| title | varchar(64) | 第 N 周考勤 |
| mode | varchar(32) | CODE/GEO |
| start_at | datetime |
| end_at | datetime |
| code | varchar(16) | 如果是口令签到 |

**attendance_records**
| id | bigint PK |
| session_id | bigint |
| student_id | bigint |
| status | varchar(16) | PRESENT/LATE/ABSENT |
| checkin_at | datetime |
| checkout_at | datetime |
| location | varchar(128) |

**leave_requests**
| id | bigint PK |
| session_id | bigint |
| student_id | bigint |
| reason | text |
| attachment_id | bigint |
| status | varchar(16) | PENDING/APPROVED/REJECTED |
| reviewed_by | bigint |
| reviewed_at | datetime |

### 3.5 讨论与公告
**discussions**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| class_id | bigint |
| author_id | bigint |
| title | varchar(128) |
| content | longtext |
| pin | tinyint | 置顶 |
| status | tinyint | 1 正常 |

**comments**
| id | bigint PK |
| discussion_id | bigint |
| parent_id | bigint nullable |
| author_id | bigint |
| content | text |

**announcements**
| id | bigint PK |
| class_id | bigint |
| author_id | bigint |
| title | varchar(128) |
| content | text |
| visible_from | datetime |
| visible_to | datetime |

### 3.6 通知与文件
**notifications**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| event_type | varchar(64) | e.g. ASSIGNMENT_DUE |
| payload | json | 事件数据 |

**user_notifications**
| id | bigint PK |
| notification_id | bigint |
| user_id | bigint |
| status | varchar(16) | UNREAD/READ |
| read_at | datetime |

**files**
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK |
| file_name | varchar(255) |
| file_size | bigint |
| mime_type | varchar(64) |
| storage_path | varchar(255) |
| uploader_id | bigint |

## 4. 约束与索引
- 所有 FK 建立对应索引，如 `idx_assignments_class_id`。
- 高频查询组合索引示例：
  - `submissions (assignment_id, student_id)`。
  - `attendance_records (session_id, student_id)`。
  - `user_notifications (user_id, status)`。
- 软删除字段 `deleted` 建议 tinyint + 组合索引 `(entity_id, deleted)`.

## 5. 视图与统计
- `vw_student_progress`：汇总学生在课程中的作业完成度、平均得分、出勤率。
- `vw_class_attendance`：每次考勤的出勤统计。
- 视图依据 `assignments/submissions/grades/attendance_records` 组合查询，供统计接口使用。

## 6. 初始化与种子数据
1. roles 表：`STUDENT`、`TEACHER`、`ADMIN`。
2. 默认管理员账号、示例课程/班级。
3. 演示环境插入 2~3 条作业、讨论帖，方便 Demo。

## 7. 迁移与版本策略
- 使用 Flyway/Liquibase 管理版本，命名规则 `V{日期}_{模块}.sql`。
- 迭代 1 完成 `users/courses/class/assignment/attendance` 核心表；迭代 2 新增讨论、公告、通知等表。

> 若后续新增模块（如考试、刷题系统），优先复用 `assignments/submissions` 模型，并在文档中追加章节说明。***

