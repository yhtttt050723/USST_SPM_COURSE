package com.usst.spm.demo.dto;

import java.time.LocalDateTime;

/**
 * 成绩历史响应DTO
 */
public class GradeHistoryResponse {
    private Long id;
    private Long gradeId;
    private Long submissionId;
    private Integer oldScore;
    private Integer newScore;
    private String oldFeedback;
    private String newFeedback;
    private String changeReason;
    private Long operatorId; // 操作人ID
    private String operatorRole; // 操作人角色（TEACHER/ADMIN）
    private String operatorName; // 操作人姓名
    private LocalDateTime changedAt;

    public GradeHistoryResponse() {
    }

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

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}

