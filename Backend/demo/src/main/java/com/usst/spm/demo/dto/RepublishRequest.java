package com.usst.spm.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 重新发布作业请求DTO
 */
public class RepublishRequest {
    /**
     * 新的截止时间（必填）
     */
    private LocalDateTime newDueAt;
    
    /**
     * 是否直接发布（true=直接发布为PUBLISHED，false=保存为DRAFT）
     */
    private Boolean publishImmediately = true;
    
    /**
     * 是否继承附件（true=复制原作业附件，false=清空附件）
     */
    private Boolean inheritAttachments = true;
    
    /**
     * 新的附件ID列表（如果inheritAttachments=false，使用此列表）
     */
    private List<Long> attachmentIds;
    
    /**
     * 是否允许修改描述等信息（可选，用于扩展）
     */
    private String newDescription;
    
    /**
     * 重新发布原因（可选，用于记录）
     */
    private String republishReason;

    public LocalDateTime getNewDueAt() {
        return newDueAt;
    }

    public void setNewDueAt(LocalDateTime newDueAt) {
        this.newDueAt = newDueAt;
    }

    public Boolean getPublishImmediately() {
        return publishImmediately;
    }

    public void setPublishImmediately(Boolean publishImmediately) {
        this.publishImmediately = publishImmediately;
    }

    public Boolean getInheritAttachments() {
        return inheritAttachments;
    }

    public void setInheritAttachments(Boolean inheritAttachments) {
        this.inheritAttachments = inheritAttachments;
    }

    public List<Long> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<Long> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public void setNewDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    public String getRepublishReason() {
        return republishReason;
    }

    public void setRepublishReason(String republishReason) {
        this.republishReason = republishReason;
    }
}

