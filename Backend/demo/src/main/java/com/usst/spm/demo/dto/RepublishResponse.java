package com.usst.spm.demo.dto;

/**
 * 重新发布作业响应DTO
 */
public class RepublishResponse {
    private Long newAssignmentId;
    private Integer version;
    private String status;
    private String message;

    public RepublishResponse() {
    }

    public RepublishResponse(Long newAssignmentId, Integer version, String status, String message) {
        this.newAssignmentId = newAssignmentId;
        this.version = version;
        this.status = status;
        this.message = message;
    }

    public Long getNewAssignmentId() {
        return newAssignmentId;
    }

    public void setNewAssignmentId(Long newAssignmentId) {
        this.newAssignmentId = newAssignmentId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

