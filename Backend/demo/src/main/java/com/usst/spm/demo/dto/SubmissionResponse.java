package com.usst.spm.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    
    // 成绩信息（如果已批改）
    private Integer score;
    private String feedback;
    private Boolean released;
    
    // 文件列表
    private List<FileInfo> files;
    
    public static class FileInfo {
        private Long id;
        private String fileName;
        private String originalName;
        private Long fileSize;
        private String mimeType;
        
        public FileInfo() {}
        
        public FileInfo(Long id, String fileName, String originalName, Long fileSize, String mimeType) {
            this.id = id;
            this.fileName = fileName;
            this.originalName = originalName;
            this.fileSize = fileSize;
            this.mimeType = mimeType;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }

    public SubmissionResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }
}

