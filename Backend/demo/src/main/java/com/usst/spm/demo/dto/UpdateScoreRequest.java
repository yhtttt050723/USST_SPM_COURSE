package com.usst.spm.demo.dto;

/**
 * 修改成绩请求DTO
 */
public class UpdateScoreRequest {
    /**
     * 新分数（必填）
     */
    private Integer newScore;
    
    /**
     * 变更原因（必填）
     */
    private String reason;
    
    /**
     * 新评语（可选）
     */
    private String feedback;

    public Integer getNewScore() {
        return newScore;
    }

    public void setNewScore(Integer newScore) {
        this.newScore = newScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

