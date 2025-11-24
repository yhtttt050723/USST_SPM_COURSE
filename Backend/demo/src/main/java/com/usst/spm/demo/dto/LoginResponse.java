package com.usst.spm.demo.dto;

public class LoginResponse {
    private String studentNo;
    private String name;
    private String role;
    private String token;

    public LoginResponse(String studentNo, String name, String role, String token) {
        this.studentNo = studentNo;
        this.name = name;
        this.role = role;
        this.token = token;
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

    public String getToken() { // 新增 getToken 方法
        return token;
    }
}