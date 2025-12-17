package com.usst.spm.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 作业附件关联实体
 */
@Entity
@Table(name = "assignment_files")
public class AssignmentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Integer deleted;

    public AssignmentFile() {
        this.createdAt = LocalDateTime.now();
        this.deleted = 0;
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

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

