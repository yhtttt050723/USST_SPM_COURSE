package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.CourseResponse;
import com.usst.spm.demo.dto.InviteCreateRequest;
import com.usst.spm.demo.dto.JoinCourseRequest;
import com.usst.spm.demo.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;
    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    private String requireStudentNo(HttpServletRequest request) {
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return studentNo;
    }

    /**
     * 获取我能访问的课程列表
     */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> listMyCourses(HttpServletRequest request) {
        String studentNo = requireStudentNo(request);
        List<CourseResponse> courses = courseService.listMyCourses(studentNo);
        return ResponseEntity.ok(courses);
    }

    /**
     * 课程详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long id,
            HttpServletRequest request) {
        String studentNo = requireStudentNo(request);
        CourseResponse course = courseService.getCourse(studentNo, id);
        return ResponseEntity.ok(course);
    }

    /**
     * 教师生成邀请码
     */
    @PostMapping("/{id}/invites")
    public ResponseEntity<Map<String, Object>> createInvite(
            @PathVariable Long id,
            @RequestBody(required = false) InviteCreateRequest body,
            HttpServletRequest request) {
        String studentNo = requireStudentNo(request);
        log.info("[invite-ctrl] create invite, studentNo={}, courseId={}, body={}", studentNo, id, body);
        Map<String, Object> resp = courseService.generateInvite(studentNo, id, body == null ? new InviteCreateRequest() : body);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * 教师失效邀请码
     */
    @DeleteMapping("/{id}/invites/{code}")
    public ResponseEntity<Void> revokeInvite(
            @PathVariable Long id,
            @PathVariable String code,
            HttpServletRequest request) {
        String studentNo = requireStudentNo(request);
        courseService.revokeInvite(studentNo, id, code);
        return ResponseEntity.noContent().build();
    }

    /**
     * 学生/教师通过邀请码加入课程
     */
    @PostMapping("/join")
    public ResponseEntity<CourseResponse> joinCourse(
            @RequestBody JoinCourseRequest body,
            HttpServletRequest request) {
        String studentNo = requireStudentNo(request);
        CourseResponse course = courseService.joinByCode(studentNo, body == null ? null : body.getCode());
        return ResponseEntity.ok(course);
    }
}

