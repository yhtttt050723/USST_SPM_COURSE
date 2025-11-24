-- 迁移脚本：添加 submission_files 表
-- 如果表已存在，此脚本不会报错

USE spm_course;

-- 创建文件表（如果不存在）
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

-- 创建提交文件关联表（如果不存在）
CREATE TABLE IF NOT EXISTS submission_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_sub_file_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
  CONSTRAINT fk_sub_file_file FOREIGN KEY (file_id) REFERENCES files(id)
);

