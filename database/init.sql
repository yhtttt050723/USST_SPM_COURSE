-- 初始化数据库与用户
CREATE DATABASE IF NOT EXISTS spm_course CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'spm_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON spm_course.* TO 'spm_user'@'%';
FLUSH PRIVILEGES;

USE spm_course;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_no VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  password VARCHAR(255) DEFAULT NULL,
  role ENUM('STUDENT','TEACHER','ADMIN') NOT NULL DEFAULT 'STUDENT',
  avatar_url VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
);

-- 2. 课程表
CREATE TABLE IF NOT EXISTS course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  code VARCHAR(32),
  semester VARCHAR(32),
  description TEXT,
  teacher_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- 作业表（含版本化与审计字段）
CREATE TABLE IF NOT EXISTS assignments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  description TEXT,
  type VARCHAR(32),
  total_score INT DEFAULT 100,
  allow_resubmit TINYINT DEFAULT 0,
  max_resubmit_count INT DEFAULT 0,
  due_at DATETIME,
  -- 版本化字段
  version INT DEFAULT 1,
  origin_id BIGINT,
  parent_assignment_id BIGINT,
  published_at DATETIME,
  -- 状态：DRAFT / PUBLISHED / CLOSED / ARCHIVED
  status VARCHAR(16) DEFAULT 'DRAFT',
  -- 审计字段
  created_by BIGINT,
  updated_by BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  deleted_at DATETIME,
  CONSTRAINT fk_assign_course FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE IF NOT EXISTS submissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  content LONGTEXT,
  status VARCHAR(16) DEFAULT 'SUBMITTED',
  submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  resubmit_count INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_sub_assign FOREIGN KEY (assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_sub_student FOREIGN KEY (student_id) REFERENCES users(id)
);

-- 文件表
CREATE TABLE IF NOT EXISTS files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(255) NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  storage_path VARCHAR(512) NOT NULL,
  mime_type VARCHAR(128),
  file_size BIGINT,
  uploader_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES users(id)
);

-- 提交文件关联表
CREATE TABLE IF NOT EXISTS submission_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_sub_file_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_sub_file_file FOREIGN KEY (file_id) REFERENCES files(id)
);

CREATE TABLE IF NOT EXISTS grades (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  scorer_id BIGINT,
  score INT,
  feedback TEXT,
  change_reason TEXT,
  released TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_grade_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_grade_scorer FOREIGN KEY (scorer_id) REFERENCES users(id)
);

-- 作业附件表
CREATE TABLE IF NOT EXISTS assignment_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_assignment_file_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_assignment_file_file FOREIGN KEY (file_id) REFERENCES files(id)
);

-- 成绩变更历史
CREATE TABLE IF NOT EXISTS grade_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  grade_id BIGINT NOT NULL,
  submission_id BIGINT NOT NULL,
  scorer_id BIGINT,
  old_score INT,
  new_score INT,
  old_feedback TEXT,
  new_feedback TEXT,
  change_reason TEXT NOT NULL,
  operator_id BIGINT NOT NULL,
  operator_role VARCHAR(16) NOT NULL,
  changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_grade_history_grade FOREIGN KEY (grade_id) REFERENCES grades(id),
  CONSTRAINT fk_grade_history_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_grade_history_scorer FOREIGN KEY (scorer_id) REFERENCES users(id),
  CONSTRAINT fk_grade_history_operator FOREIGN KEY (operator_id) REFERENCES users(id)
);

-- 4. 出勤
CREATE TABLE IF NOT EXISTS attendance_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  title VARCHAR(64),
  mode VARCHAR(16) DEFAULT 'CODE',
  start_at DATETIME,
  end_at DATETIME,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  code VARCHAR(4) NOT NULL,
  start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_att_course FOREIGN KEY (course_id) REFERENCES course(id),
  CONSTRAINT fk_att_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS attendance_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  status VARCHAR(16) DEFAULT 'PRESENT',
  checkin_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  checkout_at DATETIME,
  location VARCHAR(128),
  result VARCHAR(16) DEFAULT 'SUCCESS',
  remark VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_rec_session FOREIGN KEY (session_id) REFERENCES attendance_sessions(id),
  CONSTRAINT fk_rec_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT uk_attendance_record UNIQUE (session_id, student_id)
);

-- 5. 初始化数据
-- 教师账号：学号 T0001，密码 123456
INSERT INTO users (student_no, name, role, password, status)
VALUES ('T0001', '欧广宇', 'TEACHER', '123456', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), role = VALUES(role), password = VALUES(password), status = VALUES(status);

INSERT INTO course (name, code, semester, description, teacher_id)
VALUES (
  '项目管理与过程改进',
  'SPM2025',
  '2025-2026-1',
  '课程实践平台',
  (SELECT id FROM users WHERE student_no = 'T0001')
)
ON DUPLICATE KEY UPDATE name = VALUES(name), code = VALUES(code), semester = VALUES(semester);

ALTER USER 'spm_user'@'%' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;

INSERT INTO users (student_no, name, password, role, status)
VALUES ('2335062224', '闫鹤天', '123456', 'STUDENT', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), password = VALUES(password), role = VALUES(role), status = VALUES(status);

-- 6. 讨论区表
CREATE TABLE IF NOT EXISTS discussions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  author_role ENUM('STUDENT','TEACHER','ADMIN') NOT NULL DEFAULT 'STUDENT',
  title VARCHAR(255) NOT NULL,
  content TEXT,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  is_pinned TINYINT NOT NULL DEFAULT 0,
  allow_comment TINYINT NOT NULL DEFAULT 1,
  reply_count INT NOT NULL DEFAULT 0,
  last_reply_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0,
  deleted_at DATETIME,
  deleted_by BIGINT,
  CONSTRAINT fk_discussion_course FOREIGN KEY (course_id) REFERENCES course(id),
  CONSTRAINT fk_discussion_author FOREIGN KEY (author_id) REFERENCES users(id),
  CONSTRAINT fk_discussion_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_discussion_updated_by FOREIGN KEY (updated_by) REFERENCES users(id),
  CONSTRAINT fk_discussion_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  discussion_id BIGINT NOT NULL,
  parent_id BIGINT,
  author_id BIGINT NOT NULL,
  author_role ENUM('STUDENT','TEACHER','ADMIN') NOT NULL DEFAULT 'STUDENT',
  content TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0,
  deleted_at DATETIME,
  deleted_by BIGINT,
  CONSTRAINT fk_comment_discussion FOREIGN KEY (discussion_id) REFERENCES discussions(id),
  CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id),
  CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id),
  CONSTRAINT fk_comment_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_comment_updated_by FOREIGN KEY (updated_by) REFERENCES users(id),
  CONSTRAINT fk_comment_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id)
);

--6讨论与回复表
CREATE TABLE discussions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  title VARCHAR(255),
  content TEXT,
  reply_count INT DEFAULT 0,
  view_count INT DEFAULT 0,
  is_pinned TINYINT(1) DEFAULT 0,
  is_locked TINYINT(1) DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  deleted TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE discussion_replies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  discussion_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  parent_reply_id BIGINT NULL,
  content TEXT,
  created_at DATETIME,
  updated_at DATETIME,
  deleted TINYINT(1) DEFAULT 0,
  CONSTRAINT fk_discussion_reply_discussion
    FOREIGN KEY (discussion_id) REFERENCES discussions(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_discussion_reply_parent
    FOREIGN KEY (parent_reply_id) REFERENCES discussion_replies(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--7公告表
CREATE TABLE announcements (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  title VARCHAR(255),
  content TEXT,
  is_pinned TINYINT(1) DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  deleted TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;