package com.usst.spm.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DiscussionReplyResponse {
    private Long id;
    private Long discussionId;
    private Long authorId;
    private String authorName;
    private String authorNo;
    private Long parentReplyId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DiscussionReplyResponse> replies; // 子回复列表

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorNo() {
        return authorNo;
    }

    public void setAuthorNo(String authorNo) {
        this.authorNo = authorNo;
    }

    public Long getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Long parentReplyId) {
        this.parentReplyId = parentReplyId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public List<DiscussionReplyResponse> getReplies() {
        return replies;
    }

    public void setReplies(List<DiscussionReplyResponse> replies) {
        this.replies = replies;
    }
}

