package com.usst.spm.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 编辑作业请求DTO
 */
public class AssignmentUpdateRequest {
    private String title;
    private String description;
    private String type;
    private Integer totalScore;
    private Boolean allowResubmit;
    private Integer maxResubmitCount;
    private LocalDateTime dueAt;
    private List<Long> attachmentIds; // 作业附件ID列表

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

    public Integer getMaxResubmitCount() {
        return maxResubmitCount;
    }

    public void setMaxResubmitCount(Integer maxResubmitCount) {
        this.maxResubmitCount = maxResubmitCount;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public List<Long> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<Long> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }
}

