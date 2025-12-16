package com.usst.spm.demo.dto;

import java.time.LocalDateTime;

public class CourseResponse {
    private Long id;
    private String name;
    private String code;
    private String academicYear;
    private String term;
    private String semester;
    private String description;
    private Long teacherId;
    private String inviteCode;
    private LocalDateTime inviteExpireAt;
    /**
     * 当前用户在本课程中的角色（TEACHER/TA/STUDENT/UNKNOWN）
     */
    private String roleInCourse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public LocalDateTime getInviteExpireAt() {
        return inviteExpireAt;
    }

    public void setInviteExpireAt(LocalDateTime inviteExpireAt) {
        this.inviteExpireAt = inviteExpireAt;
    }

    public String getRoleInCourse() {
        return roleInCourse;
    }

    public void setRoleInCourse(String roleInCourse) {
        this.roleInCourse = roleInCourse;
    }
}

