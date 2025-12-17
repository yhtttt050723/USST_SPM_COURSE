-- 初始化数据库与用户
CREATE DATABASE IF NOT EXISTS spm_course CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'spm_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON spm_course.* TO 'spm_user'@'%';
FLUSH PRIVILEGES;

USE spm_course;
-- 统一当前连接的字符集与排序规则，避免 1267 Illegal mix of collations
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

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

CREATE TABLE IF NOT EXISTS course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  code VARCHAR(32),
  -- 学年，例如 2025-2026
  academic_year VARCHAR(32),
  -- 学期：UPPER=上学期, LOWER=下学期
  term ENUM('UPPER','LOWER') DEFAULT 'UPPER',
  -- 兼容旧字段（可选）
  semester VARCHAR(32),
  description TEXT,
  teacher_id BIGINT,
  -- 当前有效的邀请码（便于快速校验）
  invite_code VARCHAR(16),
  invite_expire_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- 课程邀请码历史表
CREATE TABLE IF NOT EXISTS course_invite_codes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  code VARCHAR(16) NOT NULL,
  expire_at DATETIME,
  max_use INT DEFAULT 0,           -- 0 表示不限制
  used_count INT DEFAULT 0,
  active TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_invite_code UNIQUE (code),
  CONSTRAINT fk_invite_course FOREIGN KEY (course_id) REFERENCES course(id)
);

-- 课程成员表
CREATE TABLE IF NOT EXISTS course_enrollments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  role ENUM('STUDENT','TA','TEACHER') NOT NULL DEFAULT 'STUDENT',
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_course_student UNIQUE (course_id, student_id),
  CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) REFERENCES course(id),
  CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES users(id)
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

INSERT INTO course (name, code, academic_year, term, semester, description, teacher_id, invite_code, invite_expire_at)
VALUES (
  '项目管理与过程改进',
  'SPM2025',
  '2025-2026',
  'UPPER',
  '2025-2026-1',
  '课程实践平台',
  (SELECT id FROM users WHERE student_no = 'T0001'),
  '836241',
  DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY)
)
ON DUPLICATE KEY UPDATE name = VALUES(name), code = VALUES(code), semester = VALUES(semester);

ALTER USER 'spm_user'@'%' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;

INSERT INTO users (student_no, name, password, role, status)
VALUES ('2335062224', '闫鹤天', '123456', 'STUDENT', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), password = VALUES(password), role = VALUES(role), status = VALUES(status);

-- 将示例学生加入课程
INSERT INTO course_enrollments (course_id, student_id, role, status)
SELECT c.id, u.id, 'STUDENT', 'ACTIVE'
FROM course c, users u
WHERE c.code = 'SPM2025' AND u.student_no = '2335062224'
ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- 生成一条历史邀请码记录
INSERT INTO course_invite_codes (course_id, code, expire_at, max_use, used_count, active)
SELECT c.id, '836241', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY), 200, 0, 1
FROM course c WHERE c.code = 'SPM2025'
ON DUPLICATE KEY UPDATE expire_at = VALUES(expire_at), active = 1;

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

-- 8. 测试数据：多门课程、多名学生、作业、提交、出勤、公告、讨论区

-- 8.1 多名学生（示例），使用学号避免重复
INSERT INTO users (student_no, name, role, password, status)
VALUES 
  ('S0001', '测试学生1', 'STUDENT', '123456', 1),
  ('S0002', '测试学生2', 'STUDENT', '123456', 1),
  ('S0003', '测试学生3', 'STUDENT', '123456', 1),
  ('S0004', '测试学生4', 'STUDENT', '123456', 1),
  ('S0005', '测试学生5', 'STUDENT', '123456', 1)
ON DUPLICATE KEY UPDATE
  name     = VALUES(name),
  role     = VALUES(role),
  password = VALUES(password),
  status   = VALUES(status);

-- 保存教师和部分学生 ID 变量
SET @teacher_id := (SELECT id FROM users WHERE student_no = 'T0001' LIMIT 1);
SET @stu_main   := (SELECT id FROM users WHERE student_no = '2335062224' LIMIT 1);
SET @stu1       := (SELECT id FROM users WHERE student_no = 'S0001' LIMIT 1);
SET @stu2       := (SELECT id FROM users WHERE student_no = 'S0002' LIMIT 1);
SET @stu3       := (SELECT id FROM users WHERE student_no = 'S0003' LIMIT 1);
SET @stu4       := (SELECT id FROM users WHERE student_no = 'S0004' LIMIT 1);
SET @stu5       := (SELECT id FROM users WHERE student_no = 'S0005' LIMIT 1);

-- 8.2 多门同名课程（项目管理与过程改进），同一老师不同学年/学期
INSERT INTO course (name, code, academic_year, term, semester, description, teacher_id, invite_code, invite_expire_at)
SELECT '项目管理与过程改进', 'PM2024A', '2024-2025', 'UPPER', '2024-2025-1',
       '2024秋-项目管理与过程改进A班', @teacher_id, '2024A1',
       DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 60 DAY)
WHERE NOT EXISTS (SELECT 1 FROM course WHERE code = 'PM2024A');

INSERT INTO course (name, code, academic_year, term, semester, description, teacher_id, invite_code, invite_expire_at)
SELECT '项目管理与过程改进', 'PM2024B', '2024-2025', 'LOWER', '2024-2025-2',
       '2025春-项目管理与过程改进B班', @teacher_id, '2024B1',
       DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 60 DAY)
WHERE NOT EXISTS (SELECT 1 FROM course WHERE code = 'PM2024B');

INSERT INTO course (name, code, academic_year, term, semester, description, teacher_id, invite_code, invite_expire_at)
SELECT '项目管理与过程改进', 'PM2025A', '2025-2026', 'UPPER', '2025-2026-1',
       '2025秋-项目管理与过程改进A班', @teacher_id, '2025A1',
       DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 60 DAY)
WHERE NOT EXISTS (SELECT 1 FROM course WHERE code = 'PM2025A');

-- 8.3 把上述所有“项目管理与过程改进”课程的学生名单补充完整（主学生 + S0001~S0005）
-- 主学生
INSERT INTO course_enrollments (course_id, student_id, role, status)
SELECT c.id, @stu_main, 'STUDENT', 'ACTIVE'
FROM course c
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
  AND @stu_main IS NOT NULL
ON DUPLICATE KEY UPDATE
  status  = 'ACTIVE',
  deleted = 0;

-- 其他测试学生
INSERT INTO course_enrollments (course_id, student_id, role, status)
SELECT c.id, u.id, 'STUDENT', 'ACTIVE'
FROM course c
JOIN users u ON u.student_no IN ('S0001', 'S0002', 'S0003', 'S0004', 'S0005')
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
ON DUPLICATE KEY UPDATE
  status  = 'ACTIVE',
  deleted = 0;

-- 8.4 为每门“项目管理与过程改进”课程创建两份示例作业
INSERT INTO assignments (course_id, title, description, type, total_score, allow_resubmit, max_resubmit_count, due_at, status, created_by, updated_by)
SELECT 
  c.id,
  CONCAT(c.code, ' 第一次作业'),
  '请阅读项目管理相关资料并提交一页学习心得。',
  'HOMEWORK',
  100,
  1,
  2,
  DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
  'PUBLISHED',
  @teacher_id,
  @teacher_id
FROM course c
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
  AND NOT EXISTS (
    SELECT 1 FROM assignments a 
    WHERE a.course_id = c.id AND a.title = CONCAT(c.code, ' 第一次作业')
  );

INSERT INTO assignments (course_id, title, description, type, total_score, allow_resubmit, max_resubmit_count, due_at, status, created_by, updated_by)
SELECT 
  c.id,
  CONCAT(c.code, ' 第二次作业'),
  '小组选择一个真实项目，撰写初步项目计划书并上传。',
  'PROJECT',
  100,
  0,
  0,
  DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 14 DAY),
  'PUBLISHED',
  @teacher_id,
  @teacher_id
FROM course c
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
  AND NOT EXISTS (
    SELECT 1 FROM assignments a 
    WHERE a.course_id = c.id AND a.title = CONCAT(c.code, ' 第二次作业')
  );

-- 8.5 为这些作业批量生成学生提交与部分成绩

-- 学生 S0001 与 S0002 的提交
INSERT INTO submissions (assignment_id, student_id, content, status, submitted_at, resubmit_count, created_at, updated_at)
SELECT 
  a.id,
  u.id,
  CONCAT(u.name, ' 对作业《', a.title, '》的提交内容示例。'),
  'SUBMITTED',
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
  0,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)
FROM assignments a
JOIN course c ON a.course_id = c.id
JOIN users u  ON u.student_no IN ('S0001', 'S0002')
WHERE c.name = '项目管理与过程改进'
  AND NOT EXISTS (
    SELECT 1 FROM submissions s
    WHERE s.assignment_id = a.id AND s.student_id = u.id AND s.deleted = 0
  );

-- 给 S0002 的提交打分，模拟部分已批改
INSERT INTO grades (submission_id, scorer_id, score, feedback, released, created_at, updated_at, deleted)
SELECT 
  s.id,
  @teacher_id,
  85 + (a.id % 10),
  CONCAT('作业《', a.title, '》整体完成较好，请继续保持。'),
  1,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  0
FROM submissions s
JOIN assignments a ON s.assignment_id = a.id
JOIN users u       ON s.student_id = u.id AND u.student_no = 'S0002'
WHERE NOT EXISTS (
  SELECT 1 FROM grades g WHERE g.submission_id = s.id AND g.deleted = 0
);

-- 8.6 为每门课程生成两次考勤（签到码）与签到记录

-- 每门课程两条考勤 session
INSERT INTO attendance_sessions (course_id, teacher_id, title, mode, start_at, end_at, status, code, start_time, end_time, deleted)
SELECT 
  c.id,
  @teacher_id,
  CONCAT(c.code, ' 第一次课堂考勤'),
  'CODE',
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 2 HOUR,
  'FINISHED',
  '1234',
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 2 HOUR,
  0
FROM course c
WHERE c.name = '项目管理与过程改进'
  AND NOT EXISTS (
    SELECT 1 FROM attendance_sessions s
    WHERE s.course_id = c.id AND s.title = CONCAT(c.code, ' 第一次课堂考勤')
  );

INSERT INTO attendance_sessions (course_id, teacher_id, title, mode, start_at, end_at, status, code, start_time, end_time, deleted)
SELECT 
  c.id,
  @teacher_id,
  CONCAT(c.code, ' 第二次课堂考勤'),
  'CODE',
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 2 HOUR,
  'FINISHED',
  '5678',
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 2 HOUR,
  0
FROM course c
WHERE c.name = '项目管理与过程改进'
  AND NOT EXISTS (
    SELECT 1 FROM attendance_sessions s
    WHERE s.course_id = c.id AND s.title = CONCAT(c.code, ' 第二次课堂考勤')
  );

-- 为每个考勤场次生成学生签到记录（主学生 + S0001~S0005）
INSERT INTO attendance_records (session_id, student_id, status, checkin_at, result, remark, deleted)
SELECT 
  s.id,
  e.student_id,
  CASE WHEN (e.student_id % 5) = 0 THEN 'ABSENT' ELSE 'PRESENT' END,
  s.start_time + INTERVAL 5 MINUTE,
  CASE WHEN (e.student_id % 5) = 0 THEN 'FAIL' ELSE 'SUCCESS' END,
  CASE WHEN (e.student_id % 5) = 0 THEN '未到课' ELSE '正常签到' END,
  0
FROM attendance_sessions s
JOIN course_enrollments e ON e.course_id = s.course_id AND e.status = 'ACTIVE' AND e.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM attendance_records r
  WHERE r.session_id = s.id AND r.student_id = e.student_id
);

-- 8.7 每门课程的课程公告 + 全校公告

-- 课程公告：开课通知 / 作业提醒 / 课堂讨论安排
INSERT INTO announcements (course_id, author_id, title, content, is_pinned, created_at, updated_at, deleted)
SELECT
  c.id,
  @teacher_id,
  CONCAT(c.code, ' 开课通知'),
  CONCAT('课程【', c.name, '】(', IFNULL(c.academic_year, '学年未知'), ' ', IFNULL(c.semester, ''), ') 将于本周开始，请同学们按时到课。'),
  1,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
  0
FROM course c
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
  AND NOT EXISTS (
    SELECT 1 FROM announcements a
    WHERE a.course_id = c.id 
      AND a.title = CONCAT(c.code, ' 开课通知') COLLATE utf8mb4_general_ci 
      AND a.deleted = 0
  );

INSERT INTO announcements (course_id, author_id, title, content, is_pinned, created_at, updated_at, deleted)
SELECT
  c.id,
  @teacher_id,
  CONCAT(c.code, ' 作业提醒'),
  '本周有一次作业截止，请同学们注意在平台上按时提交。',
  0,
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
  0
FROM course c
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
  AND NOT EXISTS (
    SELECT 1 FROM announcements a
    WHERE a.course_id = c.id 
      AND a.title = CONCAT(c.code, ' 作业提醒') COLLATE utf8mb4_general_ci 
      AND a.deleted = 0
  );

INSERT INTO announcements (course_id, author_id, title, content, is_pinned, created_at, updated_at, deleted)
SELECT
  c.id,
  @teacher_id,
  CONCAT(c.code, ' 课堂讨论安排'),
  '下节课将进行项目选题讨论，请每组选出一名代表进行简单汇报。',
  0,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  0
FROM course c
WHERE c.name = '项目管理与过程改进' COLLATE utf8mb4_general_ci
  AND NOT EXISTS (
    SELECT 1 FROM announcements a
    WHERE a.course_id = c.id 
      AND a.title = CONCAT(c.code, ' 课堂讨论安排') COLLATE utf8mb4_general_ci 
      AND a.deleted = 0
  );

-- 全校公告（course_id = 0，用于“全校通知”）
INSERT INTO announcements (course_id, author_id, title, content, is_pinned, created_at, updated_at, deleted)
VALUES
(0, @teacher_id, '全校通知：实验楼维护', '本周末实验楼将进行网络维护，可能短暂断网，请注意提前提交线上作业。', 1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), 0),
(0, @teacher_id, '全校通知：期中考试周安排', '期中周将安排部分课程的闭卷考试和项目汇报，请关注各课程公告。', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 8.8 每门课程的讨论区与回复

-- 老师发起的课程讨论
INSERT INTO discussions (course_id, author_id, author_role, title, content, status, is_pinned, allow_comment, reply_count, last_reply_at, created_at, updated_at, deleted)
SELECT
  c.id,
  @teacher_id,
  'TEACHER',
  CONCAT(c.code, ' 开学自我介绍'),
  '请同学们在本帖下方简单介绍自己，并说明对本课程的期待。',
  'OPEN',
  1,
  1,
  0,
  NULL,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  0
FROM course c
WHERE c.name = '项目管理与过程改进'
  AND NOT EXISTS (
    SELECT 1 FROM discussions d
    WHERE d.course_id = c.id AND d.title = CONCAT(c.code, ' 开学自我介绍') AND d.deleted = 0
  );

-- 学生的简单回复
INSERT INTO comments (discussion_id, parent_id, author_id, author_role, content, created_at, updated_at, deleted)
SELECT
  d.id,
  NULL,
  u.id,
  'STUDENT',
  CONCAT(u.name, ' 报到：大家好，我是本课程的学生，很期待和大家一起学习项目管理。'),
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  0
FROM discussions d
JOIN course c ON d.course_id = c.id
JOIN users u  ON u.student_no IN ('2335062224', 'S0001', 'S0002')
WHERE c.name = '项目管理与过程改进'
  AND d.title LIKE '%开学自我介绍%'
  AND NOT EXISTS (
    SELECT 1 FROM comments cmt
    WHERE cmt.discussion_id = d.id AND cmt.author_id = u.id AND cmt.deleted = 0
  );
