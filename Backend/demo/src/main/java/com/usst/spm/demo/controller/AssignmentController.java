package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.Assignment;
import com.usst.spm.demo.model.Grade;
import com.usst.spm.demo.model.Submission;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.AssignmentRepository;
import com.usst.spm.demo.repository.GradeRepository;
import com.usst.spm.demo.repository.SubmissionRepository;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.service.AssignmentService;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    private static final Long DEFAULT_COURSE_ID = 1L;

    private final AssignmentRepository assignmentRepository;
    private final AssignmentService assignmentService;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;

    public AssignmentController(
            AssignmentRepository assignmentRepository,
            AssignmentService assignmentService,
            SubmissionRepository submissionRepository,
            GradeRepository gradeRepository,
            UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentService = assignmentService;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建作业（教师）
     */
    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody AssignmentCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "作业标题不能为空");
        }
        if (request.getDueAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "截止时间不能为空");
        }

        Assignment assignment = new Assignment();
        assignment.setCourseId(request.getCourseId() != null ? request.getCourseId() : DEFAULT_COURSE_ID);
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setType(request.getType());
        assignment.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        assignment.setAllowResubmit(Boolean.TRUE.equals(request.getAllowResubmit()));
        assignment.setDueAt(request.getDueAt());
        assignment.setStatus(request.getDueAt().isBefore(LocalDateTime.now()) ? "ENDED" : "ONGOING");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * 获取作业列表
     * GET /api/assignments?status=all&studentId=1&role=TEACHER
     * 如果是教师，返回所有作业；如果是学生，返回带提交状态的作业列表
     */
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAssignments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String role) {
        // 如果是教师，返回所有作业（不带学生提交状态）
        if ("TEACHER".equals(role)) {
            List<Assignment> assignments = assignmentRepository.findByCourseIdAndDeleted(DEFAULT_COURSE_ID, 0);
            List<AssignmentResponse> responses = assignments.stream()
                    .map(assignment -> {
                        AssignmentResponse response = new AssignmentResponse();
                        response.setId(assignment.getId());
                        response.setCourseId(assignment.getCourseId());
                        response.setTitle(assignment.getTitle());
                        response.setDescription(assignment.getDescription());
                        response.setType(assignment.getType());
                        response.setTotalScore(assignment.getTotalScore());
                        response.setAllowResubmit(assignment.getAllowResubmit());
                        response.setDueAt(assignment.getDueAt());
                        response.setStatus(assignment.getStatus());
                        response.setCreatedAt(assignment.getCreatedAt());
                        response.setUpdatedAt(assignment.getUpdatedAt());
                        return response;
                    })
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(responses);
        }
        
        // 学生端：需要 studentId
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少学生ID参数");
        }
        List<AssignmentResponse> assignments = assignmentService.getAssignments(studentId, status);
        return ResponseEntity.ok(assignments);
    }

    /**
     * 获取作业详情
     * GET /api/assignments/{id}?studentId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少学生ID参数");
        }
        AssignmentResponse assignment = assignmentService.getAssignmentById(id, studentId);
        return ResponseEntity.ok(assignment);
    }

    /**
     * 提交作业
     * POST /api/assignments/{id}/submissions
     * Body: { "content": "...", "studentId": 1 }
     */
    @PostMapping("/{id}/submissions")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long studentId = null;
        if (request.containsKey("studentId")) {
            studentId = Long.valueOf(request.get("studentId").toString());
        }
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少学生ID");
        }

        SubmissionRequest submissionRequest = new SubmissionRequest();
        if (request.containsKey("content")) {
            submissionRequest.setContent(request.get("content").toString());
        }

        SubmissionResponse response = assignmentService.submitAssignment(id, studentId, submissionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 查看我的提交
     * GET /api/assignments/{id}/submissions/me?studentId=1
     */
    @GetMapping("/{id}/submissions/me")
    public ResponseEntity<SubmissionResponse> getMySubmission(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少学生ID参数");
        }
        SubmissionResponse submission = assignmentService.getMySubmission(id, studentId);
        return ResponseEntity.ok(submission);
    }

    /**
     * 教师批改作业
     * POST /api/assignments/{id}/submissions/{submissionId}/grade
     * Body: { "score": 92, "feedback": "...", "released": true, "teacherId": 1 }
     */
    @PostMapping("/{id}/submissions/{submissionId}/grade")
    public ResponseEntity<Grade> gradeSubmission(
            @PathVariable Long id,
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> request) {
        Long teacherId = null;
        if (request.containsKey("teacherId")) {
            teacherId = Long.valueOf(request.get("teacherId").toString());
        }
        if (teacherId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少教师ID");
        }

        GradeRequest gradeRequest = new GradeRequest();
        if (request.containsKey("score")) {
            gradeRequest.setScore(Integer.valueOf(request.get("score").toString()));
        }
        if (request.containsKey("feedback")) {
            gradeRequest.setFeedback(request.get("feedback").toString());
        }
        if (request.containsKey("released")) {
            gradeRequest.setReleased(Boolean.valueOf(request.get("released").toString()));
        }

        Grade grade = assignmentService.gradeSubmission(id, submissionId, teacherId, gradeRequest);
        return ResponseEntity.ok(grade);
    }

    /**
     * 获取我的所有成绩
     * GET /api/grades/me?studentId=1
     */
    @GetMapping("/grades/me")
    public ResponseEntity<List<AssignmentResponse>> getMyGrades(
            @RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少学生ID参数");
        }
        List<AssignmentResponse> grades = assignmentService.getMyGrades(studentId);
        return ResponseEntity.ok(grades);
    }

    /**
     * 获取作业的所有提交（教师端）
     * GET /api/assignments/{id}/submissions
     */
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<Map<String, Object>>> getSubmissions(@PathVariable Long id) {
        List<Submission> submissions = submissionRepository.findByAssignmentIdAndDeleted(id, 0);
        List<Map<String, Object>> responses = submissions.stream()
                .map(submission -> {
                    Map<String, Object> response = new java.util.HashMap<>();
                    response.put("id", submission.getId());
                    response.put("assignmentId", submission.getAssignmentId());
                    response.put("studentId", submission.getStudentId());
                    response.put("content", submission.getContent());
                    response.put("status", submission.getStatus());
                    response.put("submittedAt", submission.getSubmittedAt());
                    response.put("createdAt", submission.getCreatedAt());
                    
                    // 查询学生信息
                    Optional<User> studentOpt = userRepository.findById(submission.getStudentId());
                    if (studentOpt.isPresent()) {
                        User student = studentOpt.get();
                        response.put("studentName", student.getName());
                        response.put("studentNo", student.getStudentNo());
                    }
                    
                    // 查询成绩
                    Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
                    if (gradeOpt.isPresent()) {
                        Grade grade = gradeOpt.get();
                        response.put("score", grade.getScore());
                        response.put("feedback", grade.getFeedback());
                        response.put("released", grade.getReleased());
                        response.put("graded", Boolean.TRUE.equals(grade.getReleased()));
                    } else {
                        response.put("graded", false);
                    }
                    
                    return response;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}

