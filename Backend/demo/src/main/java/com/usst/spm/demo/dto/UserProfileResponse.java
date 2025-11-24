package com.usst.spm.demo.dto;

//用户资料响应 DTO，用于返回非敏感信息
public class UserProfileResponse {
    private Long id;
    private String studentNo;
    private String name;
    private String role;
    private Integer status;

    //从 User 实体创建 DTO
    public UserProfileResponse(Long id, String studentNo, String name, String role, Integer status) {
        this.id = id;
        this.studentNo = studentNo;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public Integer getStatus() {
        return status;
    }
}