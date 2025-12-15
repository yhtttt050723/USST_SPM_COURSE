package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.Assignment;
import com.usst.spm.demo.model.AssignmentFile;
import com.usst.spm.demo.model.File;
import com.usst.spm.demo.model.Grade;
import com.usst.spm.demo.model.Submission;
import com.usst.spm.demo.model.SubmissionFile;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.AssignmentFileRepository;
import com.usst.spm.demo.repository.AssignmentRepository;
import com.usst.spm.demo.repository.FileRepository;
import com.usst.spm.demo.repository.GradeRepository;
import com.usst.spm.demo.repository.SubmissionFileRepository;
import com.usst.spm.demo.repository.SubmissionRepository;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.service.AssignmentService;
import com.usst.spm.demo.util.AssignmentStateMachine;
import jakarta.servlet.http.HttpServletRequest;

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
    private final AssignmentFileRepository assignmentFileRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final FileRepository fileRepository;

    public AssignmentController(
            AssignmentRepository assignmentRepository,
            AssignmentService assignmentService,
            SubmissionRepository submissionRepository,
            GradeRepository gradeRepository,
            UserRepository userRepository,
            AssignmentFileRepository assignmentFileRepository,
            SubmissionFileRepository submissionFileRepository,
            FileRepository fileRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentService = assignmentService;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
        this.assignmentFileRepository = assignmentFileRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.fileRepository = fileRepository;
    }

    /**
     * 获取当前用户ID（从JWT token中获取）
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        Optional<User> userOpt = userRepository.findByStudentNo(studentNo);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return userOpt.get().getId();
    }

    /**
     * 检查是否为教师
     */
    private boolean isTeacher(String studentNo) {
        Optional<User> userOpt = userRepository.findByStudentNo(studentNo);
        return userOpt.isPresent() && "TEACHER".equals(userOpt.get().getRole());
    }

    /**
     * 创建作业（教师）
     * POST /api/assignments
     * Body: AssignmentCreateRequest { "title": "...", "description": "...", "dueAt": "...", "attachmentIds": [...], "maxResubmitCount": 3 }
     * 返回：AssignmentResponse（状态默认为DRAFT）
     */
    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestBody AssignmentCreateRequest request,
            HttpServletRequest httpRequest) {
        // 权限校验：只有教师可以创建作业
        String studentNo = (String) httpRequest.getAttribute("studentNo");
        if (studentNo == null || !isTeacher(studentNo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有教师可以创建作业");
        }

        Long teacherId = getCurrentUserId(httpRequest);
        AssignmentResponse response = assignmentService.createAssignment(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        // 如果是教师，返回所有作业（带统计信息）
        if ("TEACHER".equals(role)) {
            // 获取学生总数（role为STUDENT的用户数）
            long totalStudents = userRepository.findAll().stream()
                    .filter(user -> "STUDENT".equals(user.getRole()) && (user.getDeleted() == null || user.getDeleted() == 0))
                    .count();
            
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
                        
                        // 设置学生总数
                        response.setTotalStudents((int) totalStudents);
                        
                        // 计算提交人数（deleted 为 0 或 null 均视为有效）
                        List<Submission> submissions = submissionRepository.findActiveByAssignmentId(assignment.getId());
                        response.setSubmissionCount(submissions.size());
                        
                        // 计算已批改人数（有成绩且已发布的）
                        long gradedCount = submissions.stream()
                                .mapToLong(submission -> {
                                    Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
                                    if (gradeOpt.isPresent() && Boolean.TRUE.equals(gradeOpt.get().getReleased())) {
                                        return 1;
                                    }
                                    return 0;
                                })
                                .sum();
                        response.setGradedCount((int) gradedCount);
                        
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
     * GET /api/assignments/{id}?studentId=1 (学生端需要studentId)
     * GET /api/assignments/{id} (教师端不需要studentId)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @PathVariable Long id,
            @RequestParam(required = false) Long studentId,
            @RequestAttribute(required = false) String studentNo) {
        // 如果提供了studentId，使用studentId（学生端）
        // 如果没有提供studentId，则返回基础作业信息（教师端）
        if (studentId != null) {
            AssignmentResponse assignment = assignmentService.getAssignmentById(id, studentId);
            return ResponseEntity.ok(assignment);
        } else {
            // 教师端：只返回作业基本信息，不包含学生提交状态
            Assignment assignment = assignmentRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));
            
            if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
            }
            
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
            
            return ResponseEntity.ok(response);
        }
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
        if (request.containsKey("attachmentIds")) {
            @SuppressWarnings("unchecked")
            List<Object> attachmentIdsObj = (List<Object>) request.get("attachmentIds");
            List<Long> attachmentIds = attachmentIdsObj.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(java.util.stream.Collectors.toList());
            submissionRequest.setAttachmentIds(attachmentIds);
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
        try {
            // 验证作业是否存在
            Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
            if (assignmentOpt.isEmpty() || (assignmentOpt.get().getDeleted() != null && assignmentOpt.get().getDeleted() == 1)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
            }
            
            // 查询所有学生（用于展示未提交的学生状态）
            List<User> allStudents = userRepository.findAll().stream()
                    .filter(u -> "STUDENT".equals(u.getRole()) && (u.getDeleted() == null || u.getDeleted() == 0))
                    .toList();
            Map<Long, User> studentMap = new HashMap<>();
            allStudents.forEach(s -> studentMap.put(s.getId(), s));
            System.out.println("[DEBUG] getSubmissions - assignmentId=" + id + ", allStudents=" + allStudents.size());

            // 查询该作业的所有提交（deleted 为 0 或 null 均视为有效）
            List<Submission> submissions = submissionRepository.findActiveByAssignmentId(id);
            System.out.println("[DEBUG] getSubmissions - submissions found=" + submissions.size());

            List<Map<String, Object>> responses = new ArrayList<>();
            Set<Long> submittedStudentIds = new HashSet<>();

            // 先把已提交的学生填充
            for (Submission submission : submissions) {
                System.out.println("[DEBUG] submission id=" + submission.getId()
                        + ", studentId=" + submission.getStudentId()
                        + ", content=" + submission.getContent());
                Map<String, Object> response = new HashMap<>();
                User student = studentMap.get(submission.getStudentId());

                response.put("studentId", submission.getStudentId());
                response.put("studentName", student != null && student.getName() != null ? student.getName() : "");
                response.put("studentNo", student != null && student.getStudentNo() != null ? student.getStudentNo() : "");

                response.put("id", submission.getId());
                response.put("assignmentId", submission.getAssignmentId());
                response.put("content", submission.getContent() != null ? submission.getContent() : "");
                response.put("status", submission.getStatus() != null ? submission.getStatus() : "SUBMITTED");
                response.put("submittedAt", submission.getSubmittedAt());
                response.put("createdAt", submission.getCreatedAt());
                response.put("resubmitCount", submission.getResubmitCount() != null ? submission.getResubmitCount() : 0);

                // 附件列表
                List<SubmissionFile> submissionFiles = submissionFileRepository.findActiveBySubmissionId(submission.getId());
                List<Map<String, Object>> attachmentList = new ArrayList<>();
                for (SubmissionFile sf : submissionFiles) {
                    fileRepository.findById(sf.getFileId()).ifPresent(f -> {
                        if (f.getDeleted() == null || f.getDeleted() == 0) {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("fileId", f.getId());
                            fileInfo.put("fileName", f.getFileName());
                            fileInfo.put("originalName", f.getOriginalName());
                            fileInfo.put("storagePath", f.getStoragePath());
                            attachmentList.add(fileInfo);
                        }
                    });
                }
                System.out.println("[DEBUG] submission id=" + submission.getId()
                        + ", attachment count=" + attachmentList.size());
                response.put("attachments", attachmentList);

                // 查询成绩
                Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
                if (gradeOpt.isPresent()) {
                    Grade grade = gradeOpt.get();
                    response.put("score", grade.getScore());
                    response.put("feedback", grade.getFeedback() != null ? grade.getFeedback() : "");
                    response.put("released", grade.getReleased() != null ? grade.getReleased() : false);
                    response.put("graded", Boolean.TRUE.equals(grade.getReleased()));
                } else {
                    response.put("score", null);
                    response.put("feedback", "");
                    response.put("released", false);
                    response.put("graded", false);
                }

                submittedStudentIds.add(submission.getStudentId());
                responses.add(response);
            }

            // 再补充未提交的学生
            for (User student : allStudents) {
                if (submittedStudentIds.contains(student.getId())) {
                    continue;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("studentId", student.getId());
                response.put("studentName", student.getName() != null ? student.getName() : "");
                response.put("studentNo", student.getStudentNo() != null ? student.getStudentNo() : "");
                response.put("id", null);
                response.put("assignmentId", id);
                response.put("content", "");
                response.put("status", "NOT_SUBMITTED");
                response.put("submittedAt", null);
                response.put("createdAt", null);
                response.put("resubmitCount", 0);
                response.put("score", null);
                response.put("feedback", "");
                response.put("released", false);
                response.put("graded", false);
                response.put("attachments", Collections.emptyList());
                responses.add(response);
            }

            System.out.println("[DEBUG] getSubmissions - response size=" + responses.size());
            if (!responses.isEmpty()) {
                Map<String, Object> first = responses.get(0);
                System.out.println("[DEBUG] first response sample: studentId=" + first.get("studentId")
                        + ", status=" + first.get("status")
                        + ", id=" + first.get("id")
                        + ", attachments=" + first.get("attachments"));
            }

            return ResponseEntity.ok(responses);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("获取提交列表时出错: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "获取提交列表失败: " + e.getMessage());
        }
    }
}

