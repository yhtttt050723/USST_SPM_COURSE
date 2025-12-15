package com.usst.spm.demo.dto;

public class DiscussionReplyCreateRequest {
    private String content;
    private Long parentReplyId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Long parentReplyId) {
        this.parentReplyId = parentReplyId;
    }
}

