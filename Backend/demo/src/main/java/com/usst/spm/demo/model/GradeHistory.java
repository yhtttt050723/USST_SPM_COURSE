package com.usst.spm.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 成绩变更历史实体
 * 记录每次成绩修改的详细信息，支持回溯
 */
@Entity
@Table(name = "grade_history")
public class GradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "scorer_id")
    private Long scorerId;

    @Column(name = "old_score")
    private Integer oldScore;

    @Column(name = "new_score")
    private Integer newScore;

    @Column(name = "old_feedback", columnDefinition = "TEXT")
    private String oldFeedback;

    @Column(name = "new_feedback", columnDefinition = "TEXT")
    private String newFeedback;

    @Column(name = "change_reason", columnDefinition = "TEXT", nullable = false)
    private String changeReason;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_role", nullable = false, length = 16)
    private String operatorRole;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private Integer deleted;

    public GradeHistory() {
        this.createdAt = LocalDateTime.now();
        this.changedAt = LocalDateTime.now();
        this.deleted = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getScorerId() {
        return scorerId;
    }

    public void setScorerId(Long scorerId) {
        this.scorerId = scorerId;
    }

    public Integer getOldScore() {
        return oldScore;
    }

    public void setOldScore(Integer oldScore) {
        this.oldScore = oldScore;
    }

    public Integer getNewScore() {
        return newScore;
    }

    public void setNewScore(Integer newScore) {
        this.newScore = newScore;
    }

    public String getOldFeedback() {
        return oldFeedback;
    }

    public void setOldFeedback(String oldFeedback) {
        this.oldFeedback = oldFeedback;
    }

    public String getNewFeedback() {
        return newFeedback;
    }

    public void setNewFeedback(String newFeedback) {
        this.newFeedback = newFeedback;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorRole() {
        return operatorRole;
    }

    public void setOperatorRole(String operatorRole) {
        this.operatorRole = operatorRole;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
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

