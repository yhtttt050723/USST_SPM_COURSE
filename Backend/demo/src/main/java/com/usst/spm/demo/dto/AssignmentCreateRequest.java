package com.usst.spm.demo.dto;

import java.time.LocalDateTime;

public class AssignmentCreateRequest {
    private Long courseId;
    private String title;
    private String description;
    private String type;
    private Integer totalScore;
    private Boolean allowResubmit;
    private LocalDateTime dueAt;

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
}

