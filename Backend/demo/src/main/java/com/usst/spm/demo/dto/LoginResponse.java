package com.usst.spm.demo.dto;

public class LoginResponse {
    private String studentNo;
    private String name;
    private String role;

    public LoginResponse(String studentNo, String name, String role) {
        this.studentNo = studentNo;
        this.name = name;
        this.role = role;
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
}