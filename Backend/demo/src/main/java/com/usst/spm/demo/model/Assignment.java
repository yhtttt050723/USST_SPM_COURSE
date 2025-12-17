package com.usst.spm.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "allow_resubmit")
    private Boolean allowResubmit;

    @Column(name = "max_resubmit_count")
    private Integer maxResubmitCount;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    /**
     * 版本号（从1开始，每次重新发布+1）
     */
    @Column(name = "version")
    private Integer version;

    /**
     * 原始作业ID（同一作业链路的根ID，用于版本化管理）
     * 如果origin_id为null，说明这是原始作业，origin_id = id
     */
    @Column(name = "origin_id")
    private Long originId;

    /**
     * 作业状态：DRAFT（草稿）、PUBLISHED（已发布）、CLOSED（已截止）、ARCHIVED（已归档）
     */
    private String status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Integer deleted;

    public Assignment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "DRAFT";
        this.deleted = 0;
        this.maxResubmitCount = 0;
        this.version = 1; // 默认版本号为1
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Boolean getAllowResubmit() {
        return allowResubmit;
    }

    public void setAllowResubmit(Boolean allowResubmit) {
        this.allowResubmit = allowResubmit;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getMaxResubmitCount() {
        return maxResubmitCount;
    }

    public void setMaxResubmitCount(Integer maxResubmitCount) {
        this.maxResubmitCount = maxResubmitCount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getOriginId() {
        return originId;
    }

    public void setOriginId(Long originId) {
        this.originId = originId;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

