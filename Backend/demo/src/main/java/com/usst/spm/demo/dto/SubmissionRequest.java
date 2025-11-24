package com.usst.spm.demo.dto;

import java.util.List;

public class SubmissionRequest {
    private String content;
    private List<Long> attachmentIds;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Long> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<Long> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }
}

