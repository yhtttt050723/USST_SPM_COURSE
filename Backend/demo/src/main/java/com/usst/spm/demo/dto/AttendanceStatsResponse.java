package com.usst.spm.demo.dto;

public class AttendanceStatsResponse {
    private Long sessionId;
    private long presentCount;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public long getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(long presentCount) {
        this.presentCount = presentCount;
    }
}

