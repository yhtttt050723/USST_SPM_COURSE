package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.AssignmentResponse;
import com.usst.spm.demo.dto.GradeRequest;
import com.usst.spm.demo.dto.SubmissionRequest;
import com.usst.spm.demo.dto.SubmissionResponse;
import com.usst.spm.demo.model.Assignment;
import com.usst.spm.demo.model.Grade;
import com.usst.spm.demo.model.Submission;
import com.usst.spm.demo.repository.AssignmentRepository;
import com.usst.spm.demo.repository.GradeRepository;
import com.usst.spm.demo.repository.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private static final Long DEFAULT_COURSE_ID = 1L;

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            GradeRepository gradeRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
    }

    /**
     * 获取作业列表（学生视角，包含提交状态和成绩）
     */
    public List<AssignmentResponse> getAssignments(Long studentId, String statusFilter) {
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndDeleted(DEFAULT_COURSE_ID, 0);
        
        return assignments.stream()
                .map(assignment -> {
                    AssignmentResponse response = convertToResponse(assignment);
                    
                    // 查询学生的提交记录
                    Optional<Submission> submissionOpt = submissionRepository
                            .findByAssignmentIdAndStudentIdAndDeleted(assignment.getId(), studentId, 0);
                    
                    if (submissionOpt.isPresent()) {
                        Submission submission = submissionOpt.get();
                        response.setSubmittedAt(submission.getSubmittedAt());
                        
                        // 查询成绩
                        Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
                        if (gradeOpt.isPresent() && Boolean.TRUE.equals(gradeOpt.get().getReleased())) {
                            Grade grade = gradeOpt.get();
                            response.setScore(grade.getScore());
                            response.setFeedback(grade.getFeedback());
                            response.setSubmissionStatus("graded");
                        } else {
                            response.setSubmissionStatus("submitted");
                        }
                    } else {
                        // 判断是否已截止
                        if (assignment.getDueAt() != null && assignment.getDueAt().isBefore(LocalDateTime.now())) {
                            response.setSubmissionStatus("ended");
                        } else {
                            response.setSubmissionStatus("progress");
                        }
                    }
                    
                    return response;
                })
                .filter(response -> {
                    if (statusFilter == null || statusFilter.equals("all")) {
                        return true;
                    }
                    return statusFilter.equals(response.getSubmissionStatus());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取作业详情
     */
    public AssignmentResponse getAssignmentById(Long assignmentId, Long studentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));
        
        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }
        
        AssignmentResponse response = convertToResponse(assignment);
        
        // 查询学生的提交记录
        Optional<Submission> submissionOpt = submissionRepository
                .findByAssignmentIdAndStudentIdAndDeleted(assignmentId, studentId, 0);
        
        if (submissionOpt.isPresent()) {
            Submission submission = submissionOpt.get();
            response.setSubmittedAt(submission.getSubmittedAt());
            
            // 查询成绩
            Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
            if (gradeOpt.isPresent() && Boolean.TRUE.equals(gradeOpt.get().getReleased())) {
                Grade grade = gradeOpt.get();
                response.setScore(grade.getScore());
                response.setFeedback(grade.getFeedback());
                response.setSubmissionStatus("graded");
            } else {
                response.setSubmissionStatus("submitted");
            }
        } else {
            response.setSubmissionStatus("progress");
        }
        
        return response;
    }

    /**
     * 提交作业
     */
    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, Long studentId, SubmissionRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));
        
        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }
        
        // 检查是否已截止
        if (assignment.getDueAt() != null && assignment.getDueAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "作业已截止，无法提交");
        }
        
        // 检查是否已有提交
        Optional<Submission> existingSubmission = submissionRepository
                .findByAssignmentIdAndStudentIdAndDeleted(assignmentId, studentId, 0);
        
        if (existingSubmission.isPresent()) {
            if (!Boolean.TRUE.equals(assignment.getAllowResubmit())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该作业不允许重复提交");
            }
            // 更新现有提交
            Submission submission = existingSubmission.get();
            submission.setContent(request.getContent());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setUpdatedAt(LocalDateTime.now());
            submission.setStatus("SUBMITTED");
            submission = submissionRepository.save(submission);
            return convertToSubmissionResponse(submission);
        } else {
            // 创建新提交
            Submission submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(studentId);
            submission.setContent(request.getContent());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("SUBMITTED");
            submission = submissionRepository.save(submission);
            return convertToSubmissionResponse(submission);
        }
    }

    /**
     * 查看我的提交
     */
    public SubmissionResponse getMySubmission(Long assignmentId, Long studentId) {
        Submission submission = submissionRepository
                .findByAssignmentIdAndStudentIdAndDeleted(assignmentId, studentId, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到提交记录"));
        
        SubmissionResponse response = convertToSubmissionResponse(submission);
        
        // 查询成绩
        Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
        if (gradeOpt.isPresent() && Boolean.TRUE.equals(gradeOpt.get().getReleased())) {
            Grade grade = gradeOpt.get();
            response.setScore(grade.getScore());
            response.setFeedback(grade.getFeedback());
            response.setReleased(true);
        }
        
        return response;
    }

    /**
     * 教师批改作业
     */
    @Transactional
    public Grade gradeSubmission(Long assignmentId, Long submissionId, Long teacherId, GradeRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在"));
        
        if (!submission.getAssignmentId().equals(assignmentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提交记录与作业不匹配");
        }
        
        if (submission.getDeleted() != null && submission.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在");
        }
        
        // 检查是否已有成绩
        Optional<Grade> existingGrade = gradeRepository.findBySubmissionIdAndDeleted(submissionId, 0);
        
        if (existingGrade.isPresent()) {
            // 更新现有成绩
            Grade grade = existingGrade.get();
            grade.setScore(request.getScore());
            grade.setFeedback(request.getFeedback());
            grade.setReleased(Boolean.TRUE.equals(request.getReleased()));
            grade.setUpdatedAt(LocalDateTime.now());
            return gradeRepository.save(grade);
        } else {
            // 创建新成绩
            Grade grade = new Grade();
            grade.setSubmissionId(submissionId);
            grade.setScorerId(teacherId);
            grade.setScore(request.getScore());
            grade.setFeedback(request.getFeedback());
            grade.setReleased(Boolean.TRUE.equals(request.getReleased()));
            return gradeRepository.save(grade);
        }
    }

    /**
     * 获取我的所有成绩
     */
    public List<AssignmentResponse> getMyGrades(Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentIdAndDeleted(studentId, 0);
        List<Long> submissionIds = submissions.stream()
                .map(Submission::getId)
                .collect(Collectors.toList());
        
        List<Grade> grades = gradeRepository.findBySubmissionIdInAndDeleted(submissionIds, 0);
        
        return submissions.stream()
                .map(submission -> {
                    Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                            .orElse(null);
                    if (assignment == null) {
                        return null;
                    }
                    
                    AssignmentResponse response = convertToResponse(assignment);
                    response.setSubmittedAt(submission.getSubmittedAt());
                    
                    Grade grade = grades.stream()
                            .filter(g -> g.getSubmissionId().equals(submission.getId()))
                            .filter(g -> Boolean.TRUE.equals(g.getReleased()))
                            .findFirst()
                            .orElse(null);
                    
                    if (grade != null) {
                        response.setScore(grade.getScore());
                        response.setFeedback(grade.getFeedback());
                        response.setSubmissionStatus("graded");
                    } else {
                        response.setSubmissionStatus("submitted");
                    }
                    
                    return response;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    /**
     * 转换 Assignment 为 AssignmentResponse
     */
    private AssignmentResponse convertToResponse(Assignment assignment) {
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
    }

    /**
     * 转换 Submission 为 SubmissionResponse
     */
    private SubmissionResponse convertToSubmissionResponse(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentId(submission.getAssignmentId());
        response.setStudentId(submission.getStudentId());
        response.setContent(submission.getContent());
        response.setStatus(submission.getStatus());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setCreatedAt(submission.getCreatedAt());
        return response;
    }
}

