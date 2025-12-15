package com.usst.spm.demo.dto;

public class CommentCreateRequest {
    private String content;
    private Long parentId; // 可选，用于回复评论

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}

