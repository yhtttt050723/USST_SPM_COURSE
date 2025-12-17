-- 作业模块增强 - 数据库迁移脚本
-- 执行前请备份数据库

USE spm_course;

-- 1. 扩展assignments表：添加版本化字段和审计字段
ALTER TABLE assignments 
ADD COLUMN IF NOT EXISTS max_resubmit_count INT DEFAULT 0 COMMENT '最大重提交次数',
ADD COLUMN IF NOT EXISTS version INT DEFAULT 1 COMMENT '版本号（从1开始）',
ADD COLUMN IF NOT EXISTS origin_id BIGINT COMMENT '原始作业ID（同一作业链路的根ID，用于版本化管理）',
ADD COLUMN IF NOT EXISTS published_at DATETIME COMMENT '发布时间',
ADD COLUMN IF NOT EXISTS created_by BIGINT COMMENT '创建人ID',
ADD COLUMN IF NOT EXISTS updated_by BIGINT COMMENT '最后更新人ID',
ADD COLUMN IF NOT EXISTS deleted_at DATETIME COMMENT '删除时间',
ADD INDEX IF NOT EXISTS idx_origin_id (origin_id),
ADD INDEX IF NOT EXISTS idx_version (version),
ADD INDEX IF NOT EXISTS idx_status (status),
ADD INDEX IF NOT EXISTS idx_created_by (created_by);

-- 2. 创建作业附件关联表（如果不存在）
CREATE TABLE IF NOT EXISTS assignment_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assignment_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_assignment_file_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
  CONSTRAINT fk_assignment_file_file FOREIGN KEY (file_id) REFERENCES files(id),
  INDEX idx_assignment_file_assignment (assignment_id),
  INDEX idx_assignment_file_file (file_id)
);

-- 3. 扩展grades表：添加变更原因字段（如果不存在）
ALTER TABLE grades 
ADD COLUMN IF NOT EXISTS change_reason TEXT COMMENT '成绩变更原因';

-- 4. 创建成绩历史表
CREATE TABLE IF NOT EXISTS grade_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  grade_id BIGINT NOT NULL COMMENT '成绩ID',
  submission_id BIGINT NOT NULL COMMENT '提交ID',
  scorer_id BIGINT COMMENT '评分教师ID',
  old_score INT COMMENT '原分数',
  new_score INT COMMENT '新分数',
  old_feedback TEXT COMMENT '原评语',
  new_feedback TEXT COMMENT '新评语',
  change_reason TEXT NOT NULL COMMENT '变更原因（必填）',
  operator_id BIGINT NOT NULL COMMENT '操作人ID',
  operator_role VARCHAR(16) NOT NULL COMMENT '操作人角色（TEACHER/ADMIN）',
  changed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_grade_history_grade FOREIGN KEY (grade_id) REFERENCES grades(id),
  CONSTRAINT fk_grade_history_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_grade_history_scorer FOREIGN KEY (scorer_id) REFERENCES users(id),
  CONSTRAINT fk_grade_history_operator FOREIGN KEY (operator_id) REFERENCES users(id),
  INDEX idx_grade_history_grade (grade_id),
  INDEX idx_grade_history_submission (submission_id),
  INDEX idx_grade_history_operator (operator_id)
);

-- 5. 扩展submissions表：添加重提交次数字段（如果不存在）
ALTER TABLE submissions 
ADD COLUMN IF NOT EXISTS resubmit_count INT DEFAULT 0 COMMENT '重提交次数';

-- 6. 更新现有数据：将status='ONGOING'的作业根据due_at判断状态
-- 注意：此操作会修改现有数据，请谨慎执行
-- UPDATE assignments 
-- SET status = CASE 
--   WHEN due_at IS NULL THEN 'DRAFT'
--   WHEN due_at < NOW() THEN 'CLOSED'
--   WHEN status = 'ONGOING' THEN 'PUBLISHED'
--   ELSE status
-- END
-- WHERE deleted = 0;

-- 7. 将status字段默认值改为DRAFT（新创建的作业默认为草稿）
ALTER TABLE assignments 
MODIFY COLUMN status VARCHAR(16) DEFAULT 'DRAFT' COMMENT '作业状态：DRAFT（草稿）、PUBLISHED（已发布）、CLOSED（已截止）、ARCHIVED（已归档）';

