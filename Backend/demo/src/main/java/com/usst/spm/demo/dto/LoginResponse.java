package com.usst.spm.demo.dto;

public class LoginResponse {
    private Long id;
    private String studentNo;
    private String name;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(Long id, String studentNo, String name, String role) {
        this.id = id;
        this.studentNo = studentNo;
        this.name = name;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}