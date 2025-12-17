package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.AssignmentCreateRequest;
import com.usst.spm.demo.dto.AssignmentResponse;
import com.usst.spm.demo.dto.AssignmentUpdateRequest;
import com.usst.spm.demo.dto.GradeHistoryResponse;
import com.usst.spm.demo.dto.GradeRequest;
import com.usst.spm.demo.dto.RepublishRequest;
import com.usst.spm.demo.dto.RepublishResponse;
import com.usst.spm.demo.dto.SubmissionRequest;
import com.usst.spm.demo.dto.SubmissionResponse;
import com.usst.spm.demo.dto.UpdateScoreRequest;
import com.usst.spm.demo.model.Assignment;
import com.usst.spm.demo.model.AssignmentFile;
import com.usst.spm.demo.model.File;
import com.usst.spm.demo.model.Grade;
import com.usst.spm.demo.model.GradeHistory;
import com.usst.spm.demo.model.Submission;
import com.usst.spm.demo.model.SubmissionFile;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.AssignmentFileRepository;
import com.usst.spm.demo.repository.AssignmentRepository;
import com.usst.spm.demo.repository.FileRepository;
import com.usst.spm.demo.repository.GradeHistoryRepository;
import com.usst.spm.demo.repository.GradeRepository;
import com.usst.spm.demo.repository.SubmissionFileRepository;
import com.usst.spm.demo.repository.SubmissionRepository;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.util.AssignmentStateMachine;
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

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final FileRepository fileRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final UserRepository userRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            GradeRepository gradeRepository,
            FileRepository fileRepository,
            SubmissionFileRepository submissionFileRepository,
            AssignmentFileRepository assignmentFileRepository,
            GradeHistoryRepository gradeHistoryRepository,
            UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.fileRepository = fileRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.assignmentFileRepository = assignmentFileRepository;
        this.gradeHistoryRepository = gradeHistoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取作业列表（学生视角，包含提交状态和成绩）
     */
    public List<AssignmentResponse> getAssignments(Long courseId, Long studentId, String statusFilter) {
        if (courseId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少课程ID");
        }
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndDeleted(courseId, 0);
        
        return assignments.stream()
                .map(assignment -> {
                    AssignmentResponse response = convertToResponse(assignment);
                    
                    // 查询学生的提交记录
                    Optional<Submission> submissionOpt = submissionRepository
                            .findActiveByAssignmentIdAndStudentId(assignment.getId(), studentId);
                    
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
                .findActiveByAssignmentIdAndStudentId(assignmentId, studentId);
        
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
                .findActiveByAssignmentIdAndStudentId(assignmentId, studentId);
        
        if (existingSubmission.isPresent()) {
            if (!Boolean.TRUE.equals(assignment.getAllowResubmit())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该作业不允许重复提交");
            }
            
            // 检查重提交次数限制
            Submission submission = existingSubmission.get();
            int currentResubmitCount = submission.getResubmitCount() != null ? submission.getResubmitCount() : 0;
            int maxResubmitCount = assignment.getMaxResubmitCount() != null ? assignment.getMaxResubmitCount() : 0;
            
            if (maxResubmitCount > 0 && currentResubmitCount >= maxResubmitCount) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "已达到最大重提交次数限制: " + maxResubmitCount);
            }
            
            // 更新现有提交
            submission.setContent(request.getContent());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setUpdatedAt(LocalDateTime.now());
            submission.setStatus("SUBMITTED");
            submission.setResubmitCount(currentResubmitCount + 1); // 增加重提交次数
            submission = submissionRepository.save(submission);
            
            // 处理文件关联
            if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
                // 删除旧的关联
                List<SubmissionFile> oldFiles = submissionFileRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
                for (SubmissionFile oldFile : oldFiles) {
                    oldFile.setDeleted(1);
                    submissionFileRepository.save(oldFile);
                }
                
                // 创建新的关联
                for (Long fileId : request.getAttachmentIds()) {
                    SubmissionFile submissionFile = new SubmissionFile();
                    submissionFile.setSubmissionId(submission.getId());
                    submissionFile.setFileId(fileId);
                    submissionFileRepository.save(submissionFile);
                }
            }
            
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
            
            // 处理文件关联
            if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
                for (Long fileId : request.getAttachmentIds()) {
                    SubmissionFile submissionFile = new SubmissionFile();
                    submissionFile.setSubmissionId(submission.getId());
                    submissionFile.setFileId(fileId);
                    submissionFileRepository.save(submissionFile);
                }
            }
            
            return convertToSubmissionResponse(submission);
        }
    }

    /**
     * 查看我的提交
     */
    public SubmissionResponse getMySubmission(Long assignmentId, Long studentId) {
        Submission submission = submissionRepository
                .findActiveByAssignmentIdAndStudentId(assignmentId, studentId)
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
     * 教师批改作业（兼容旧接口，内部调用updateGrade）
     */
    @Transactional
    public Grade gradeSubmission(Long assignmentId, Long submissionId, Long teacherId, GradeRequest request) {
        return updateGrade(assignmentId, submissionId, teacherId, request);
    }

    /**
     * 获取我的所有成绩
     */
    public List<AssignmentResponse> getMyGrades(Long studentId) {
        List<Submission> submissions = submissionRepository.findActiveByStudentId(studentId);
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
    public AssignmentResponse convertToResponse(Assignment assignment) {
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
        // 版本化字段
        response.setVersion(assignment.getVersion());
        response.setOriginId(assignment.getOriginId());
        response.setPublishedAt(assignment.getPublishedAt());
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
        
        // 加载文件列表
        List<SubmissionFile> submissionFiles = submissionFileRepository.findBySubmissionIdAndDeleted(submission.getId(), 0);
        List<SubmissionResponse.FileInfo> fileInfos = submissionFiles.stream()
                .map(sf -> {
                    Optional<File> fileOpt = fileRepository.findById(sf.getFileId());
                    if (fileOpt.isPresent() && (fileOpt.get().getDeleted() == null || fileOpt.get().getDeleted() == 0)) {
                        File file = fileOpt.get();
                        return new SubmissionResponse.FileInfo(
                                file.getId(),
                                file.getFileName(),
                                file.getOriginalName(),
                                file.getFileSize(),
                                file.getMimeType()
                        );
                    }
                    return null;
                })
                .filter(f -> f != null)
                .collect(Collectors.toList());
        response.setFiles(fileInfos);
        
        return response;
    }

    // ==================== 作业管理方法 ====================

    /**
     * 创建作业（Draft默认）
     */
    @Transactional
    public AssignmentResponse createAssignment(AssignmentCreateRequest request, Long teacherId) {
        // 表单校验
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "作业标题不能为空");
        }
        if (request.getTotalScore() != null && request.getTotalScore() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "总分必须大于0");
        }

        Assignment assignment = new Assignment();
        if (request.getCourseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少课程ID");
        }
        assignment.setCourseId(request.getCourseId());
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setType(request.getType());
        assignment.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        assignment.setAllowResubmit(Boolean.TRUE.equals(request.getAllowResubmit()));
        assignment.setMaxResubmitCount(request.getMaxResubmitCount() != null ? request.getMaxResubmitCount() : 0);
        assignment.setDueAt(request.getDueAt());
        // 新创建的作业默认为草稿
        assignment.setStatus(AssignmentStateMachine.STATUS_DRAFT);
        // 版本化字段：新作业version=1，origin_id为null（表示这是原始作业）
        assignment.setVersion(1);
        assignment.setOriginId(null);
        // 审计字段
        assignment.setCreatedBy(teacherId);
        assignment.setUpdatedBy(teacherId);
        
        assignment = assignmentRepository.save(assignment);

        // 处理附件
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            for (Long fileId : request.getAttachmentIds()) {
                AssignmentFile assignmentFile = new AssignmentFile();
                assignmentFile.setAssignmentId(assignment.getId());
                assignmentFile.setFileId(fileId);
                assignmentFileRepository.save(assignmentFile);
            }
        }

        return convertToResponse(assignment);
    }

    /**
     * 编辑作业
     * 根据状态限制可编辑字段：
     * - DRAFT：可编辑所有字段
     * - PUBLISHED：只允许改 due_at、allow_resubmit、max_resubmit_count 等有限字段
     * - CLOSED：不允许编辑或仅允许归档
     */
    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentUpdateRequest request, Long teacherId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));

        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        String currentStatus = assignment.getStatus();
        
        // 状态校验和字段限制
        if (AssignmentStateMachine.STATUS_CLOSED.equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "已截止的作业不允许编辑，请使用归档功能");
        }
        
        if (AssignmentStateMachine.STATUS_ARCHIVED.equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "已归档的作业不允许编辑");
        }

        // DRAFT状态：可编辑所有字段
        if (AssignmentStateMachine.STATUS_DRAFT.equals(currentStatus)) {
            if (request.getTitle() != null) {
                assignment.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                assignment.setDescription(request.getDescription());
            }
            if (request.getType() != null) {
                assignment.setType(request.getType());
            }
            if (request.getTotalScore() != null) {
                if (request.getTotalScore() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "总分必须大于0");
                }
                assignment.setTotalScore(request.getTotalScore());
            }
            if (request.getAllowResubmit() != null) {
                assignment.setAllowResubmit(request.getAllowResubmit());
            }
            if (request.getMaxResubmitCount() != null) {
                assignment.setMaxResubmitCount(request.getMaxResubmitCount());
            }
            if (request.getDueAt() != null) {
                assignment.setDueAt(request.getDueAt());
                // 根据截止时间自动更新状态
                assignment.setStatus(AssignmentStateMachine.autoDetermineStatus(request.getDueAt(), assignment.getStatus()));
            }
        } 
        // PUBLISHED状态：只允许改有限字段
        else if (AssignmentStateMachine.STATUS_PUBLISHED.equals(currentStatus)) {
            // 只允许修改：截止时间、允许重提交、最大重提交次数
            if (request.getDueAt() != null) {
                assignment.setDueAt(request.getDueAt());
                // 根据截止时间自动更新状态
                assignment.setStatus(AssignmentStateMachine.autoDetermineStatus(request.getDueAt(), assignment.getStatus()));
            }
            if (request.getAllowResubmit() != null) {
                assignment.setAllowResubmit(request.getAllowResubmit());
            }
            if (request.getMaxResubmitCount() != null) {
                assignment.setMaxResubmitCount(request.getMaxResubmitCount());
            }
            
            // 不允许修改：标题、描述、类型、总分
            if (request.getTitle() != null || request.getDescription() != null 
                    || request.getType() != null || request.getTotalScore() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "已发布的作业不允许修改标题、描述、类型和总分，如需修改请撤回发布后编辑");
            }
        }

        // 记录更新人
        assignment.setUpdatedBy(teacherId);
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment = assignmentRepository.save(assignment);

        // 处理附件
        if (request.getAttachmentIds() != null) {
            // 删除旧的附件关联
            List<AssignmentFile> oldFiles = assignmentFileRepository.findByAssignmentIdAndDeleted(assignmentId, 0);
            for (AssignmentFile oldFile : oldFiles) {
                oldFile.setDeleted(1);
                assignmentFileRepository.save(oldFile);
            }

            // 创建新的附件关联
            for (Long fileId : request.getAttachmentIds()) {
                AssignmentFile assignmentFile = new AssignmentFile();
                assignmentFile.setAssignmentId(assignment.getId());
                assignmentFile.setFileId(fileId);
                assignmentFileRepository.save(assignmentFile);
            }
        }

        return convertToResponse(assignment);
    }

    /**
     * 删除作业（软删）
     * 删除规则：
     * - DRAFT：可删除
     * - PUBLISHED：若已有提交记录，不允许删除（改为归档/关闭）；若无提交可删除
     * - CLOSED：不允许删除（归档）
     */
    @Transactional
    public void deleteAssignment(Long assignmentId, Long teacherId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));

        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        String status = assignment.getStatus();
        
        // DRAFT：可删除
        if (AssignmentStateMachine.STATUS_DRAFT.equals(status)) {
            assignment.setDeleted(1);
            assignment.setDeletedAt(LocalDateTime.now());
            assignment.setUpdatedBy(teacherId);
            assignment.setUpdatedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
            return;
        }
        
        // PUBLISHED：检查是否有提交记录
        if (AssignmentStateMachine.STATUS_PUBLISHED.equals(status)) {
        List<Submission> submissions = submissionRepository.findActiveByAssignmentId(assignmentId);
            if (!submissions.isEmpty()) {
                // 有提交记录，不允许删除，建议归档
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "该作业已有提交记录，不允许删除。如需删除，请先归档作业");
            }
            // 无提交记录，可以删除
            assignment.setDeleted(1);
            assignment.setDeletedAt(LocalDateTime.now());
            assignment.setUpdatedBy(teacherId);
            assignment.setUpdatedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
            return;
        }
        
        // CLOSED：不允许删除（归档）
        if (AssignmentStateMachine.STATUS_CLOSED.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "已截止的作业不允许删除，请使用归档功能");
        }
        
        // ARCHIVED：可删除
        if (AssignmentStateMachine.STATUS_ARCHIVED.equals(status)) {
            assignment.setDeleted(1);
            assignment.setDeletedAt(LocalDateTime.now());
            assignment.setUpdatedBy(teacherId);
            assignment.setUpdatedAt(LocalDateTime.now());
            assignmentRepository.save(assignment);
            return;
        }
        
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "当前状态的作业不能删除，状态: " + status);
    }

    /**
     * 复制作业
     * 创建一个新的草稿作业，复制原作业的所有信息
     */
    @Transactional
    public AssignmentResponse copyAssignment(Long assignmentId, Long teacherId) {
        Assignment original = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));

        if (original.getDeleted() != null && original.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        // 创建新作业（复制作业是独立的，不是版本化）
        Assignment copy = new Assignment();
        copy.setCourseId(original.getCourseId());
        copy.setTitle(original.getTitle() + " (副本)");
        copy.setDescription(original.getDescription());
        copy.setType(original.getType());
        copy.setTotalScore(original.getTotalScore());
        copy.setAllowResubmit(original.getAllowResubmit());
        copy.setMaxResubmitCount(original.getMaxResubmitCount());
        copy.setDueAt(original.getDueAt());
        copy.setStatus(AssignmentStateMachine.STATUS_DRAFT); // 复制的作业默认为草稿
        // 复制作业是独立的，version=1，origin_id=null（表示这是原始作业）
        copy.setVersion(1);
        copy.setOriginId(null);
        // 审计字段
        copy.setCreatedBy(teacherId);
        copy.setUpdatedBy(teacherId);

        copy = assignmentRepository.save(copy);

        // 复制附件
        List<AssignmentFile> originalFiles = assignmentFileRepository.findByAssignmentIdAndDeleted(assignmentId, 0);
        for (AssignmentFile originalFile : originalFiles) {
            AssignmentFile copyFile = new AssignmentFile();
            copyFile.setAssignmentId(copy.getId());
            copyFile.setFileId(originalFile.getFileId());
            assignmentFileRepository.save(copyFile);
        }

        return convertToResponse(copy);
    }

    /**
     * 发布作业
     * 将草稿或已截止的作业发布
     */
    @Transactional
    public AssignmentResponse publishAssignment(Long assignmentId, Long teacherId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));

        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        // 状态校验
        if (!AssignmentStateMachine.canPublish(assignment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "当前状态的作业不能发布，状态: " + assignment.getStatus());
        }

        // 必填字段校验
        if (assignment.getTitle() == null || assignment.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "作业标题不能为空");
        }
        if (assignment.getDueAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "截止时间不能为空");
        }

        // 更新状态
        assignment.setStatus(AssignmentStateMachine.STATUS_PUBLISHED);
        assignment.setPublishedAt(LocalDateTime.now()); // 记录发布时间
        assignment.setUpdatedBy(teacherId); // 记录更新人
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment = assignmentRepository.save(assignment);

        return convertToResponse(assignment);
    }

    /**
     * 撤回发布
     * 将已发布的作业撤回为草稿（已截止的作业不能撤回）
     */
    @Transactional
    public AssignmentResponse unpublishAssignment(Long assignmentId, Long teacherId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));

        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        // 状态校验
        if (!AssignmentStateMachine.canUnpublish(assignment.getStatus(), assignment.getDueAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "当前状态的作业不能撤回发布，状态: " + assignment.getStatus());
        }

        // 更新状态
        assignment.setStatus(AssignmentStateMachine.STATUS_DRAFT);
        assignment.setUpdatedBy(teacherId); // 记录更新人
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment = assignmentRepository.save(assignment);

        return convertToResponse(assignment);
    }

    /**
     * 重新发布作业（版本化实现）
     * 基于原作业复制生成新版本记录，保留旧版本的提交记录
     * 
     * 业务规则：
     * 1. 原作业必须是PUBLISHED或CLOSED状态（DRAFT不需要重新发布）
     * 2. 如果原作业未截止但要重新发布，需要记录原因
     * 3. 创建新记录：origin_id = 原作业的origin_id（如果原作业已有origin_id，则沿用；否则origin_id = 原作业id）
     * 4. version = 同一origin_id下的最大version + 1
     * 5. 状态设为DRAFT或PUBLISHED（取决于publishImmediately参数）
     * 6. 旧版本的提交记录保留且仍关联旧assignment_id
     */
    @Transactional
    public RepublishResponse republishAssignment(Long assignmentId, RepublishRequest request, Long teacherId) {
        // 1. 查询原作业
        Assignment original = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));

        if (original.getDeleted() != null && original.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        // 2. 前置校验：原作业必须是PUBLISHED或CLOSED状态
        String originalStatus = original.getStatus();
        if (!AssignmentStateMachine.STATUS_PUBLISHED.equals(originalStatus) 
                && !AssignmentStateMachine.STATUS_CLOSED.equals(originalStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "只有已发布或已截止的作业可以重新发布，当前状态: " + originalStatus);
        }

        // 3. 如果原作业未截止但要重新发布，记录原因（允许但需要记录）
        boolean isNotExpired = original.getDueAt() != null 
                && original.getDueAt().isAfter(LocalDateTime.now());
        if (isNotExpired && (request.getRepublishReason() == null || request.getRepublishReason().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "原作业尚未截止，重新发布需要填写原因");
        }

        // 4. 确定origin_id：如果原作业已有origin_id，则沿用；否则origin_id = 原作业id
        Long originId = original.getOriginId() != null ? original.getOriginId() : original.getId();

        // 5. 查询同一origin_id下的最大版本号（并发安全：在事务中查询并+1）
        List<Assignment> existingVersions = assignmentRepository.findByOriginIdAndDeletedOrderByVersionDesc(originId, 0);
        int newVersionNumber = 1;
        if (!existingVersions.isEmpty()) {
            // 找到最大版本号
            int maxVersion = existingVersions.stream()
                    .mapToInt(a -> a.getVersion() != null ? a.getVersion() : 1)
                    .max()
                    .orElse(1);
            newVersionNumber = maxVersion + 1;
        }

        // 6. 创建新版本的作业
        Assignment newVersionAssignment = new Assignment();
        newVersionAssignment.setCourseId(original.getCourseId());
        newVersionAssignment.setTitle(original.getTitle());
        
        // 允许修改描述
        newVersionAssignment.setDescription(
            request.getNewDescription() != null && !request.getNewDescription().isBlank() 
                ? request.getNewDescription() 
                : original.getDescription()
        );
        
        newVersionAssignment.setType(original.getType());
        newVersionAssignment.setTotalScore(original.getTotalScore());
        newVersionAssignment.setAllowResubmit(original.getAllowResubmit());
        newVersionAssignment.setMaxResubmitCount(original.getMaxResubmitCount());
        
        // 新的截止时间（必填）
        if (request.getNewDueAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新的截止时间不能为空");
        }
        newVersionAssignment.setDueAt(request.getNewDueAt());
        
        // 版本化字段
        newVersionAssignment.setVersion(newVersionNumber);
        newVersionAssignment.setOriginId(originId);
        
        // 状态：根据publishImmediately决定
        boolean publishImmediately = request.getPublishImmediately() != null 
                ? request.getPublishImmediately() 
                : true; // 默认直接发布
        if (publishImmediately) {
            newVersionAssignment.setStatus(AssignmentStateMachine.STATUS_PUBLISHED);
            newVersionAssignment.setPublishedAt(LocalDateTime.now());
        } else {
            newVersionAssignment.setStatus(AssignmentStateMachine.STATUS_DRAFT);
        }
        
        // 审计字段
        newVersionAssignment.setCreatedBy(teacherId);
        newVersionAssignment.setUpdatedBy(teacherId);

        newVersionAssignment = assignmentRepository.save(newVersionAssignment);

        // 7. 处理附件
        if (Boolean.TRUE.equals(request.getInheritAttachments())) {
            // 继承附件：复制原作业的附件
            List<AssignmentFile> originalFiles = assignmentFileRepository.findByAssignmentIdAndDeleted(assignmentId, 0);
            for (AssignmentFile originalFile : originalFiles) {
                AssignmentFile newFile = new AssignmentFile();
                newFile.setAssignmentId(newVersionAssignment.getId());
                newFile.setFileId(originalFile.getFileId());
                assignmentFileRepository.save(newFile);
            }
        } else if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            // 使用新的附件列表
            for (Long fileId : request.getAttachmentIds()) {
                AssignmentFile newFile = new AssignmentFile();
                newFile.setAssignmentId(newVersionAssignment.getId());
                newFile.setFileId(fileId);
                assignmentFileRepository.save(newFile);
            }
        }
        // 如果inheritAttachments=false且没有提供新附件列表，则附件为空（清空附件）

        // 8. 返回响应
        RepublishResponse response = new RepublishResponse();
        response.setNewAssignmentId(newVersionAssignment.getId());
        response.setVersion(newVersionAssignment.getVersion());
        response.setStatus(newVersionAssignment.getStatus());
        response.setMessage("作业重新发布成功，新版本号: " + newVersionAssignment.getVersion());
        
        return response;
    }

    /**
     * 修改学生成绩（改分接口）
     */
    @Transactional
    public Grade updateScore(Long submissionId, UpdateScoreRequest request, Long operatorId, String operatorRole) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在"));

        if (submission.getDeleted() != null && submission.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在");
        }

        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在"));
        if (assignment.getDeleted() != null && assignment.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "作业不存在");
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "变更原因不能为空");
        }

        int totalScore = assignment.getTotalScore() != null ? assignment.getTotalScore() : 100;
        if (request.getNewScore() == null || request.getNewScore() < 0 || request.getNewScore() > totalScore) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "分数必须在 [0, " + totalScore + "] 范围内");
        }

        Optional<Grade> gradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submissionId, 0);
        Grade grade;
        if (gradeOpt.isPresent()) {
            grade = gradeOpt.get();

            GradeHistory history = new GradeHistory();
            history.setGradeId(grade.getId());
            history.setSubmissionId(submissionId);
            history.setScorerId(grade.getScorerId());
            history.setOldScore(grade.getScore());
            history.setOldFeedback(grade.getFeedback());
            history.setNewScore(request.getNewScore());
            history.setNewFeedback(request.getFeedback());
            history.setChangeReason(request.getReason());
            history.setOperatorId(operatorId);
            history.setOperatorRole(operatorRole != null ? operatorRole : "TEACHER");
            gradeHistoryRepository.save(history);

            grade.setScore(request.getNewScore());
            if (request.getFeedback() != null) {
                grade.setFeedback(request.getFeedback());
            }
            grade.setChangeReason(request.getReason());
            grade.setUpdatedAt(LocalDateTime.now());
            grade = gradeRepository.save(grade);
        } else {
            grade = new Grade();
            grade.setSubmissionId(submissionId);
            grade.setScorerId(operatorId);
            grade.setScore(request.getNewScore());
            grade.setFeedback(request.getFeedback());
            grade.setReleased(false);
            grade.setChangeReason(request.getReason());
            grade = gradeRepository.save(grade);

            GradeHistory history = new GradeHistory();
            history.setGradeId(grade.getId());
            history.setSubmissionId(submissionId);
            history.setScorerId(operatorId);
            history.setOldScore(null);
            history.setOldFeedback(null);
            history.setNewScore(request.getNewScore());
            history.setNewFeedback(request.getFeedback());
            history.setChangeReason(request.getReason());
            history.setOperatorId(operatorId);
            history.setOperatorRole(operatorRole != null ? operatorRole : "TEACHER");
            gradeHistoryRepository.save(history);
        }

        return grade;
    }

    /**
     * 查询成绩历史
     */
    public List<GradeHistoryResponse> getScoreHistory(Long submissionId, Long userId, String userRole) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在"));
        if (submission.getDeleted() != null && submission.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在");
        }

        if ("STUDENT".equals(userRole)) {
            if (userId == null || !submission.getStudentId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看其他学生的成绩历史");
            }
        }

        List<GradeHistory> histories = gradeHistoryRepository
                .findBySubmissionIdAndDeletedOrderByChangedAtDesc(submissionId, 0);

        return histories.stream().map(h -> {
            GradeHistoryResponse r = new GradeHistoryResponse();
            r.setId(h.getId());
            r.setGradeId(h.getGradeId());
            r.setSubmissionId(h.getSubmissionId());
            r.setOldScore(h.getOldScore());
            r.setNewScore(h.getNewScore());
            r.setOldFeedback(h.getOldFeedback());
            r.setNewFeedback(h.getNewFeedback());
            r.setChangeReason(h.getChangeReason());
            r.setOperatorId(h.getOperatorId());
            r.setOperatorRole(h.getOperatorRole());
            r.setChangedAt(h.getChangedAt());
            Optional<User> op = userRepository.findById(h.getOperatorId());
            r.setOperatorName(op.map(User::getName).orElse("未知"));
            return r;
        }).collect(Collectors.toList());
    }

    /**
     * 修改成绩（带历史记录）
     */
    @Transactional
    public Grade updateGrade(Long assignmentId, Long submissionId, Long teacherId, GradeRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在"));

        if (!submission.getAssignmentId().equals(assignmentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提交记录与作业不匹配");
        }

        if (submission.getDeleted() != null && submission.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在");
        }

        // 查找现有成绩
        Optional<Grade> existingGradeOpt = gradeRepository.findBySubmissionIdAndDeleted(submissionId, 0);

        if (existingGradeOpt.isPresent()) {
            // 更新现有成绩，记录历史
            Grade grade = existingGradeOpt.get();
            
            // 创建历史记录
            GradeHistory history = new GradeHistory();
            history.setGradeId(grade.getId());
            history.setSubmissionId(submissionId);
            history.setScorerId(grade.getScorerId());
            history.setOldScore(grade.getScore());
            history.setOldFeedback(grade.getFeedback());
            history.setNewScore(request.getScore());
            history.setNewFeedback(request.getFeedback());
            history.setChangeReason(request.getChangeReason() != null ? request.getChangeReason() : "成绩修改");
            history.setOperatorId(teacherId);
            // 获取操作人角色
            Optional<User> operatorOpt = userRepository.findById(teacherId);
            if (operatorOpt.isPresent()) {
                history.setOperatorRole(operatorOpt.get().getRole());
            } else {
                history.setOperatorRole("TEACHER"); // 默认
            }
            gradeHistoryRepository.save(history);

            // 更新成绩
            grade.setScore(request.getScore());
            grade.setFeedback(request.getFeedback());
            grade.setReleased(Boolean.TRUE.equals(request.getReleased()));
            grade.setChangeReason(request.getChangeReason());
            grade.setUpdatedAt(LocalDateTime.now());
            // 注意：Grade实体如果有updated_by字段，也需要设置
            return gradeRepository.save(grade);
        } else {
            // 创建新成绩（首次批改）
            Grade grade = new Grade();
            grade.setSubmissionId(submissionId);
            grade.setScorerId(teacherId);
            grade.setScore(request.getScore());
            grade.setFeedback(request.getFeedback());
            grade.setReleased(Boolean.TRUE.equals(request.getReleased()));
            grade.setChangeReason(request.getChangeReason());
            return gradeRepository.save(grade);
        }
    }

    /**
     * 获取成绩历史（旧方法，保留兼容性）
     * @deprecated 使用 getScoreHistory 替代
     */
    @Deprecated
    public List<GradeHistoryResponse> getGradeHistory(Long submissionId) {
        return getScoreHistory(submissionId, null, "ADMIN"); // 管理员权限查看
    }
}

