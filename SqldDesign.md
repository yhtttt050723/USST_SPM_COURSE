# 项目管理与过程改进课程网站数据库设计文档

## 1. 数据库概述

### 1.1 数据库信息
- **数据库类型**: MySQL 8.0
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **数据库名**: spm_db

### 1.2 设计原则
- 遵循第三范式，减少数据冗余
- 合理使用索引，提高查询性能
- 设置适当的外键约束，保证数据完整性
- 使用软删除，保留历史数据

## 2. 表结构设计

### 2.1 用户管理相关表

#### 2.1.1 用户表 (users)
```sql
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL DEFAULT 'STUDENT' COMMENT '用户角色',
    student_id VARCHAR(20) NULL COMMENT '学号',
    avatar VARCHAR(255) NULL COMMENT '头像URL',
    status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态',
    last_login_time DATETIME NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME NULL COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_student_id ON users(student_id);
```

#### 2.1.2 用户会话表 (user_sessions)
```sql
CREATE TABLE user_sessions (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(500) NOT NULL COMMENT 'JWT Token',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 索引
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(token);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
```

### 2.2 课程管理相关表

#### 2.2.1 课程表 (courses)
```sql
CREATE TABLE courses (
    course_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    description TEXT NULL COMMENT '课程描述',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期',
    status ENUM('ACTIVE', 'INACTIVE', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE' COMMENT '课程状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME NULL COMMENT '删除时间',
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 索引
CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);
CREATE INDEX idx_courses_semester ON courses(semester);
CREATE INDEX idx_courses_status ON courses(status);
```

#### 2.2.2 课程公告表 (course_announcements)
```sql
CREATE TABLE course_announcements (
    announcement_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    author_id BIGINT NOT NULL COMMENT '发布者ID',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否置顶',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME NULL COMMENT '删除时间',
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程公告表';

-- 索引
CREATE INDEX idx_course_announcements_course_id ON course_announcements(course_id);
CREATE INDEX idx_course_announcements_author_id ON course_announcements(author_id);
CREATE INDEX idx_course_announcements_is_pinned ON course_announcements(is_pinned);
```

#### 2.2.3 课程学生关系表 (course_students)
```sql
CREATE TABLE course_students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status ENUM('ACTIVE', 'DROPPED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    UNIQUE KEY uk_course_student (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学生关系表';

-- 索引
CREATE INDEX idx_course_students_course_id ON course_students(course_id);
CREATE INDEX idx_course_students_student_id ON course_students(student_id);
CREATE INDEX idx_course_students_status ON course_students(status);
```

### 2.3 题目管理相关表

#### 2.3.1 题目分类表 (question_categories)
```sql
CREATE TABLE question_categories (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description TEXT NULL COMMENT '分类描述',
    parent_id BIGINT NULL COMMENT '父分类ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES question_categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目分类表';

-- 索引
CREATE INDEX idx_question_categories_parent_id ON question_categories(parent_id);
CREATE INDEX idx_question_categories_sort_order ON question_categories(sort_order);
```

#### 2.3.2 题目表 (questions)
```sql
CREATE TABLE questions (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    title VARCHAR(500) NOT NULL COMMENT '题目标题',
    content TEXT NOT NULL COMMENT '题目内容',
    type ENUM('MULTIPLE_CHOICE', 'FILL_BLANK', 'ESSAY', 'TRUE_FALSE') NOT NULL COMMENT '题目类型',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL DEFAULT 'MEDIUM' COMMENT '难度等级',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    options JSON NULL COMMENT '选项(JSON格式)',
    correct_answer TEXT NULL COMMENT '正确答案',
    explanation TEXT NULL COMMENT '题目解析',
    points INT NOT NULL DEFAULT 1 COMMENT '分值',
    author_id BIGINT NOT NULL COMMENT '创建者ID',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME NULL COMMENT '删除时间',
    FOREIGN KEY (category_id) REFERENCES question_categories(category_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 索引
CREATE INDEX idx_questions_category_id ON questions(category_id);
CREATE INDEX idx_questions_type ON questions(type);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_questions_author_id ON questions(author_id);
CREATE INDEX idx_questions_status ON questions(status);
```

#### 2.3.3 题目标签表 (question_tags)
```sql
CREATE TABLE question_tags (
    tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    color VARCHAR(7) NULL COMMENT '标签颜色',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签表';

-- 索引
CREATE INDEX idx_question_tags_tag_name ON question_tags(tag_name);
```

#### 2.3.4 题目标签关系表 (question_tag_relations)
```sql
CREATE TABLE question_tag_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_question_tag (question_id, tag_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES question_tags(tag_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关系表';

-- 索引
CREATE INDEX idx_question_tag_relations_question_id ON question_tag_relations(question_id);
CREATE INDEX idx_question_tag_relations_tag_id ON question_tag_relations(tag_id);
```

### 2.4 刷题系统相关表

#### 2.4.1 练习记录表 (practice_sessions)
```sql
CREATE TABLE practice_sessions (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '练习会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NULL COMMENT '课程ID',
    session_name VARCHAR(100) NULL COMMENT '练习名称',
    category_id BIGINT NULL COMMENT '分类ID',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NULL COMMENT '难度等级',
    question_count INT NOT NULL COMMENT '题目数量',
    time_limit INT NULL COMMENT '时间限制(秒)',
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    completed_at DATETIME NULL COMMENT '完成时间',
    total_score INT NULL COMMENT '总分',
    correct_count INT NULL COMMENT '正确数量',
    time_spent INT NULL COMMENT '用时(秒)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES question_categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习记录表';

-- 索引
CREATE INDEX idx_practice_sessions_user_id ON practice_sessions(user_id);
CREATE INDEX idx_practice_sessions_course_id ON practice_sessions(course_id);
CREATE INDEX idx_practice_sessions_category_id ON practice_sessions(category_id);
CREATE INDEX idx_practice_sessions_status ON practice_sessions(status);
CREATE INDEX idx_practice_sessions_started_at ON practice_sessions(started_at);
```

#### 2.4.2 练习题目关系表 (practice_questions)
```sql
CREATE TABLE practice_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    session_id BIGINT NOT NULL COMMENT '练习会话ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    question_order INT NOT NULL COMMENT '题目顺序',
    user_answer TEXT NULL COMMENT '用户答案',
    is_correct BOOLEAN NULL COMMENT '是否正确',
    time_spent INT NULL COMMENT '用时(秒)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (session_id) REFERENCES practice_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习题目关系表';

-- 索引
CREATE INDEX idx_practice_questions_session_id ON practice_questions(session_id);
CREATE INDEX idx_practice_questions_question_id ON practice_questions(question_id);
CREATE INDEX idx_practice_questions_question_order ON practice_questions(question_order);
```

#### 2.4.3 错题本表 (wrong_questions)
```sql
CREATE TABLE wrong_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '错题ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    wrong_answer TEXT NULL COMMENT '错误答案',
    review_count INT NOT NULL DEFAULT 0 COMMENT '复习次数',
    last_reviewed_at DATETIME NULL COMMENT '最后复习时间',
    is_mastered BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已掌握',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_question (user_id, question_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本表';

-- 索引
CREATE INDEX idx_wrong_questions_user_id ON wrong_questions(user_id);
CREATE INDEX idx_wrong_questions_question_id ON wrong_questions(question_id);
CREATE INDEX idx_wrong_questions_is_mastered ON wrong_questions(is_mastered);
```

### 2.5 作业管理相关表

#### 2.5.1 作业表 (assignments)
```sql
CREATE TABLE assignments (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作业ID',
    title VARCHAR(200) NOT NULL COMMENT '作业标题',
    description TEXT NULL COMMENT '作业描述',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    author_id BIGINT NOT NULL COMMENT '创建者ID',
    due_date DATETIME NOT NULL COMMENT '截止时间',
    time_limit INT NULL COMMENT '时间限制(分钟)',
    max_attempts INT NULL COMMENT '最大尝试次数',
    total_points INT NOT NULL DEFAULT 100 COMMENT '总分',
    status ENUM('DRAFT', 'ACTIVE', 'CLOSED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    is_published BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否发布',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME NULL COMMENT '删除时间',
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- 索引
CREATE INDEX idx_assignments_course_id ON assignments(course_id);
CREATE INDEX idx_assignments_author_id ON assignments(author_id);
CREATE INDEX idx_assignments_due_date ON assignments(due_date);
CREATE INDEX idx_assignments_status ON assignments(status);
CREATE INDEX idx_assignments_is_published ON assignments(is_published);
```

#### 2.5.2 作业题目关系表 (assignment_questions)
```sql
CREATE TABLE assignment_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    question_order INT NOT NULL COMMENT '题目顺序',
    points INT NOT NULL DEFAULT 1 COMMENT '分值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业题目关系表';

-- 索引
CREATE INDEX idx_assignment_questions_assignment_id ON assignment_questions(assignment_id);
CREATE INDEX idx_assignment_questions_question_id ON assignment_questions(question_id);
CREATE INDEX idx_assignment_questions_question_order ON assignment_questions(question_order);
```

#### 2.5.3 作业提交表 (assignment_submissions)
```sql
CREATE TABLE assignment_submissions (
    submission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    attempt_number INT NOT NULL DEFAULT 1 COMMENT '尝试次数',
    status ENUM('DRAFT', 'SUBMITTED', 'GRADED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    submitted_at DATETIME NULL COMMENT '提交时间',
    graded_at DATETIME NULL COMMENT '批改时间',
    total_score INT NULL COMMENT '总分',
    feedback TEXT NULL COMMENT '反馈',
    grader_id BIGINT NULL COMMENT '批改者ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (grader_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

-- 索引
CREATE INDEX idx_assignment_submissions_assignment_id ON assignment_submissions(assignment_id);
CREATE INDEX idx_assignment_submissions_student_id ON assignment_submissions(student_id);
CREATE INDEX idx_assignment_submissions_status ON assignment_submissions(status);
CREATE INDEX idx_assignment_submissions_submitted_at ON assignment_submissions(submitted_at);
```

#### 2.5.4 作业答案表 (assignment_answers)
```sql
CREATE TABLE assignment_answers (
    answer_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    submission_id BIGINT NOT NULL COMMENT '提交ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer TEXT NULL COMMENT '答案内容',
    score INT NULL COMMENT '得分',
    feedback TEXT NULL COMMENT '反馈',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (submission_id) REFERENCES assignment_submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业答案表';

-- 索引
CREATE INDEX idx_assignment_answers_submission_id ON assignment_answers(submission_id);
CREATE INDEX idx_assignment_answers_question_id ON assignment_answers(question_id);
```

#### 2.5.5 作业附件表 (assignment_attachments)
```sql
CREATE TABLE assignment_attachments (
    attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    submission_id BIGINT NOT NULL COMMENT '提交ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) NULL COMMENT '文件类型',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    FOREIGN KEY (submission_id) REFERENCES assignment_submissions(submission_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业附件表';

-- 索引
CREATE INDEX idx_assignment_attachments_submission_id ON assignment_attachments(submission_id);
```

### 2.6 出勤管理相关表

#### 2.6.1 出勤记录表 (attendance_records)
```sql
CREATE TABLE attendance_records (
    attendance_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出勤记录ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    checkin_time DATETIME NOT NULL COMMENT '签到时间',
    checkout_time DATETIME NULL COMMENT '签退时间',
    location VARCHAR(200) NULL COMMENT '签到位置',
    status ENUM('PRESENT', 'LATE', 'ABSENT', 'LEAVE') NOT NULL DEFAULT 'PRESENT' COMMENT '出勤状态',
    duration INT NULL COMMENT '出勤时长(分钟)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出勤记录表';

-- 索引
CREATE INDEX idx_attendance_records_course_id ON attendance_records(course_id);
CREATE INDEX idx_attendance_records_student_id ON attendance_records(student_id);
CREATE INDEX idx_attendance_records_checkin_time ON attendance_records(checkin_time);
CREATE INDEX idx_attendance_records_status ON attendance_records(status);
```

#### 2.6.2 请假申请表 (leave_requests)
```sql
CREATE TABLE leave_requests (
    request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '请假申请ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    leave_date DATE NOT NULL COMMENT '请假日期',
    reason TEXT NOT NULL COMMENT '请假原因',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '申请状态',
    approver_id BIGINT NULL COMMENT '审批者ID',
    approved_at DATETIME NULL COMMENT '审批时间',
    feedback TEXT NULL COMMENT '审批反馈',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (approver_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';

-- 索引
CREATE INDEX idx_leave_requests_student_id ON leave_requests(student_id);
CREATE INDEX idx_leave_requests_course_id ON leave_requests(course_id);
CREATE INDEX idx_leave_requests_leave_date ON leave_requests(leave_date);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
```

#### 2.6.3 请假附件表 (leave_attachments)
```sql
CREATE TABLE leave_attachments (
    attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    request_id BIGINT NOT NULL COMMENT '请假申请ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) NULL COMMENT '文件类型',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    FOREIGN KEY (request_id) REFERENCES leave_requests(request_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假附件表';

-- 索引
CREATE INDEX idx_leave_attachments_request_id ON leave_attachments(request_id);
```

### 2.7 成绩管理相关表

#### 2.7.1 成绩表 (grades)
```sql
CREATE TABLE grades (
    grade_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    assignment_id BIGINT NULL COMMENT '作业ID',
    grade_type ENUM('ASSIGNMENT', 'EXAM', 'ATTENDANCE', 'PARTICIPATION') NOT NULL COMMENT '成绩类型',
    score INT NOT NULL COMMENT '得分',
    max_score INT NOT NULL COMMENT '满分',
    percentage DECIMAL(5,2) NOT NULL COMMENT '百分比',
    grade_letter VARCHAR(5) NULL COMMENT '等级',
    feedback TEXT NULL COMMENT '反馈',
    grader_id BIGINT NULL COMMENT '评分者ID',
    graded_at DATETIME NULL COMMENT '评分时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE SET NULL,
    FOREIGN KEY (grader_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- 索引
CREATE INDEX idx_grades_student_id ON grades(student_id);
CREATE INDEX idx_grades_course_id ON grades(course_id);
CREATE INDEX idx_grades_assignment_id ON grades(assignment_id);
CREATE INDEX idx_grades_grade_type ON grades(grade_type);
CREATE INDEX idx_grades_grader_id ON grades(grader_id);
```

### 2.8 消息通知相关表

#### 2.8.1 消息表 (messages)
```sql
CREATE TABLE messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    recipient_id BIGINT NULL COMMENT '接收者ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type ENUM('NOTIFICATION', 'ASSIGNMENT', 'EXAM', 'ANNOUNCEMENT', 'SYSTEM') NOT NULL COMMENT '消息类型',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    status ENUM('DRAFT', 'SENT', 'READ', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    is_broadcast BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否广播消息',
    target_roles JSON NULL COMMENT '目标角色(JSON格式)',
    target_courses JSON NULL COMMENT '目标课程(JSON格式)',
    sent_at DATETIME NULL COMMENT '发送时间',
    read_at DATETIME NULL COMMENT '阅读时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 索引
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX idx_messages_message_type ON messages(message_type);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_is_broadcast ON messages(is_broadcast);
CREATE INDEX idx_messages_sent_at ON messages(sent_at);
```

#### 2.8.2 消息附件表 (message_attachments)
```sql
CREATE TABLE message_attachments (
    attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) NULL COMMENT '文件类型',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息附件表';

-- 索引
CREATE INDEX idx_message_attachments_message_id ON message_attachments(message_id);
```

### 2.9 文件管理相关表

#### 2.9.1 文件表 (files)
```sql
CREATE TABLE files (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小',
    file_type VARCHAR(100) NULL COMMENT '文件类型',
    mime_type VARCHAR(100) NULL COMMENT 'MIME类型',
    file_hash VARCHAR(64) NULL COMMENT '文件哈希值',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否公开',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME NULL COMMENT '删除时间',
    FOREIGN KEY (uploader_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

-- 索引
CREATE INDEX idx_files_uploader_id ON files(uploader_id);
CREATE INDEX idx_files_file_type ON files(file_type);
CREATE INDEX idx_files_is_public ON files(is_public);
CREATE INDEX idx_files_created_at ON files(created_at);
```

### 2.10 系统管理相关表

#### 2.10.1 系统配置表 (system_configs)
```sql
CREATE TABLE system_configs (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NULL COMMENT '配置值',
    config_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') NOT NULL DEFAULT 'STRING' COMMENT '配置类型',
    description TEXT NULL COMMENT '配置描述',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统配置',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 索引
CREATE INDEX idx_system_configs_config_key ON system_configs(config_key);
CREATE INDEX idx_system_configs_is_system ON system_configs(is_system);
```

#### 2.10.2 操作日志表 (operation_logs)
```sql
CREATE TABLE operation_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT NULL COMMENT '用户ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(500) NULL COMMENT '操作描述',
    request_method VARCHAR(10) NULL COMMENT '请求方法',
    request_url VARCHAR(500) NULL COMMENT '请求URL',
    request_ip VARCHAR(50) NULL COMMENT '请求IP',
    user_agent VARCHAR(500) NULL COMMENT '用户代理',
    request_params TEXT NULL COMMENT '请求参数',
    response_result TEXT NULL COMMENT '响应结果',
    execution_time INT NULL COMMENT '执行时间(毫秒)',
    status ENUM('SUCCESS', 'FAILURE', 'ERROR') NOT NULL COMMENT '操作状态',
    error_message TEXT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 索引
CREATE INDEX idx_operation_logs_user_id ON operation_logs(user_id);
CREATE INDEX idx_operation_logs_operation_type ON operation_logs(operation_type);
CREATE INDEX idx_operation_logs_status ON operation_logs(status);
CREATE INDEX idx_operation_logs_created_at ON operation_logs(created_at);
```

## 3. 数据库初始化脚本

### 3.1 创建数据库
```sql
CREATE DATABASE spm_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 3.2 插入初始数据

#### 3.2.1 插入默认用户
```sql
-- 插入管理员用户
INSERT INTO users (username, email, password, role, status) VALUES 
('系统管理员', 'admin@usst.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'ADMIN', 'ACTIVE');

-- 插入测试教师用户
INSERT INTO users (username, email, password, role, status) VALUES 
('李老师', 'teacher@usst.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'TEACHER', 'ACTIVE');

-- 插入测试学生用户
INSERT INTO users (username, email, password, role, student_id, status) VALUES 
('张三', 'student1@usst.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'STUDENT', '2021001', 'ACTIVE'),
('李四', 'student2@usst.edu.cn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'STUDENT', '2021002', 'ACTIVE');
```

#### 3.2.2 插入题目分类
```sql
INSERT INTO question_categories (category_name, description, sort_order) VALUES 
('项目管理基础', '项目管理基础知识', 1),
('项目规划', '项目规划相关题目', 2),
('项目执行', '项目执行相关题目', 3),
('项目监控', '项目监控相关题目', 4),
('项目收尾', '项目收尾相关题目', 5);
```

#### 3.2.3 插入题目标签
```sql
INSERT INTO question_tags (tag_name, color) VALUES 
('基础概念', '#FF6B6B'),
('过程组', '#4ECDC4'),
('知识领域', '#45B7D1'),
('工具技术', '#96CEB4'),
('案例分析', '#FFEAA7');
```

#### 3.2.4 插入系统配置
```sql
INSERT INTO system_configs (config_key, config_value, config_type, description, is_system) VALUES 
('system.name', '项目管理与过程改进课程网站', 'STRING', '系统名称', TRUE),
('system.version', '1.0.0', 'STRING', '系统版本', TRUE),
('system.max_file_size', '10485760', 'NUMBER', '最大文件大小(字节)', TRUE),
('system.allowed_file_types', '["pdf","doc","docx","ppt","pptx","xls","xlsx","jpg","jpeg","png","gif"]', 'JSON', '允许的文件类型', TRUE),
('system.session_timeout', '7200', 'NUMBER', '会话超时时间(秒)', TRUE);
```

## 4. 数据库优化建议

### 4.1 索引优化
- 为经常查询的字段创建索引
- 为外键字段创建索引
- 为复合查询创建复合索引
- 定期分析慢查询日志，优化查询性能

### 4.2 分区策略
- 对于大数据量表（如操作日志表），考虑按时间分区
- 对于用户相关表，考虑按用户ID分区

### 4.3 缓存策略
- 对频繁查询的配置信息进行缓存
- 对用户会话信息进行缓存
- 对统计数据进行定期缓存

### 4.4 备份策略
- 定期进行全量备份
- 定期进行增量备份
- 测试备份数据的恢复能力

## 5. 数据迁移脚本

### 5.1 版本控制
```sql
CREATE TABLE schema_migrations (
    version VARCHAR(255) PRIMARY KEY,
    applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库版本控制表';
```

### 5.2 数据清理脚本
```sql
-- 清理过期的会话记录
DELETE FROM user_sessions WHERE expires_at < NOW();

-- 清理过期的操作日志（保留3个月）
DELETE FROM operation_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 清理已删除的文件记录
DELETE FROM files WHERE deleted_at IS NOT NULL AND deleted_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## 6. 数据库监控

### 6.1 性能监控
- 监控数据库连接数
- 监控慢查询
- 监控锁等待
- 监控磁盘使用率

### 6.2 数据完整性检查
- 定期检查外键约束
- 定期检查数据一致性
- 定期检查索引使用情况

### 6.3 安全监控
- 监控异常登录
- 监控敏感操作
- 监控数据访问模式
