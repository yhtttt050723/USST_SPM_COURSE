package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.service.DiscussionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/discussions")
@CrossOrigin(origins = "*")
public class DiscussionController {

    private final DiscussionService discussionService;
    private final UserRepository userRepository;

    public DiscussionController(DiscussionService discussionService, UserRepository userRepository) {
        this.discussionService = discussionService;
        this.userRepository = userRepository;
    }

    private User requireLogin(HttpServletRequest request) {
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    }

    private boolean isTeacherOrAdmin(User user) {
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        return "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return requireLogin(request).getId();
    }

    /**
     * 创建讨论帖
     * POST /api/discussions
     * Body 必须包含 courseId
     */
    @PostMapping
    public ResponseEntity<DiscussionResponse> createDiscussion(
            HttpServletRequest request,
            @RequestBody DiscussionCreateRequest createRequest) {
        User currentUser = requireLogin(request);
        Long authorId = currentUser.getId();
        
        if (createRequest.getTitle() == null || createRequest.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题不能为空");
        }
        
        if (createRequest.getCourseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少课程ID参数");
        }

        // 学生创建时忽略管理字段
        if (!isTeacherOrAdmin(currentUser)) {
            createRequest.setPinned(false);
            createRequest.setAllowComment(true);
            createRequest.setStatus("OPEN");
        }

        DiscussionResponse response = discussionService.createDiscussion(authorId, currentUser.getRole(), createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取讨论帖列表
     * GET /api/discussions?courseId=1&includeDeleted=false
     * 教师端：includeDeleted=true 可以看到所有（包括已删除的）
     * 学生端：includeDeleted=false 只能看到未删除的
     * courseId 为必填参数
     */
    @GetMapping
    public ResponseEntity<List<DiscussionResponse>> getDiscussions(
            HttpServletRequest request,
            @RequestParam(required = true) Long courseId,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        User currentUser = requireLogin(request);
        if (includeDeleted && !isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限查看已删除内容");
        }

        List<DiscussionResponse> discussions = discussionService.getDiscussions(
                courseId,
                includeDeleted,
                keyword,
                status,
                page,
                size
        );
        discussions.forEach(d -> applyDiscussionPermissions(d, currentUser));
        return ResponseEntity.ok(discussions);
    }

    /**
     * 获取讨论帖详情（包含评论）
     * GET /api/discussions/{id}?includeDeleted=false
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> getDiscussionById(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        User currentUser = requireLogin(request);
        if (includeDeleted && !isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限查看已删除内容");
        }

        DiscussionResponse discussion = discussionService.getDiscussionById(id, includeDeleted);
        applyDiscussionPermissions(discussion, currentUser);
        if (discussion.getComments() != null) {
            applyCommentPermissions(discussion.getComments(), currentUser);
        }
        return ResponseEntity.ok(discussion);
    }

    /**
     * 获取讨论帖回复分页
     * GET /api/discussions/{id}/replies?page=1&size=10
     */
    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentResponse>> getDiscussionReplies(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        User currentUser = requireLogin(request);
        if (includeDeleted && !isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限查看已删除内容");
        }
        List<CommentResponse> replies = discussionService.getCommentsPaged(id, includeDeleted, page, size);
        applyCommentPermissions(replies, currentUser);
        return ResponseEntity.ok(replies);
    }

    /**
     * 更新讨论帖
     * PUT /api/discussions/{id}
     * 教师可以修改任何讨论帖，学生只能修改自己的
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionResponse> updateDiscussion(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody DiscussionUpdateRequest updateRequest) {
        User currentUser = requireLogin(request);
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或管理员可以修改讨论帖");
        }

        DiscussionResponse response = discussionService.updateDiscussion(id, updateRequest, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 删除讨论帖（软删除）
     * DELETE /api/discussions/{id}
     * 教师可以删除任何讨论帖，学生只能删除自己的
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDiscussion(
            HttpServletRequest request,
            @PathVariable Long id) {
        User currentUser = requireLogin(request);

        DiscussionResponse existing = discussionService.getDiscussionById(id, true);
        boolean isOwner = currentUser.getId().equals(existing.getAuthorId());
        if (!isTeacherOrAdmin(currentUser) && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限删除此讨论帖");
        }

        discussionService.deleteDiscussion(id, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @PostMapping("/{id}/pin")
    public ResponseEntity<DiscussionResponse> pinDiscussion(
            HttpServletRequest request,
            @PathVariable Long id) {
        User currentUser = requireLogin(request);
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或管理员可以置顶讨论帖");
        }
        DiscussionResponse response = discussionService.pinDiscussion(id, true, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unpin")
    public ResponseEntity<DiscussionResponse> unpinDiscussion(
            HttpServletRequest request,
            @PathVariable Long id) {
        User currentUser = requireLogin(request);
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或管理员可以取消置顶");
        }
        DiscussionResponse response = discussionService.pinDiscussion(id, false, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<DiscussionResponse> closeDiscussion(
            HttpServletRequest request,
            @PathVariable Long id) {
        User currentUser = requireLogin(request);
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或管理员可以关闭评论");
        }
        DiscussionResponse response = discussionService.switchComment(id, false, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<DiscussionResponse> openDiscussion(
            HttpServletRequest request,
            @PathVariable Long id) {
        User currentUser = requireLogin(request);
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或管理员可以开启评论");
        }
        DiscussionResponse response = discussionService.switchComment(id, true, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 创建评论
     * POST /api/discussions/{discussionId}/comments
     */
    @PostMapping("/{discussionId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            HttpServletRequest request,
            @PathVariable Long discussionId,
            @RequestBody CommentCreateRequest createRequest) {
        User currentUser = requireLogin(request);
        Long authorId = currentUser.getId();
        
        if (createRequest.getContent() == null || createRequest.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评论内容不能为空");
        }

        CommentResponse response = discussionService.createComment(discussionId, authorId, currentUser.getRole(), createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 更新评论
     * PUT /api/discussions/{discussionId}/comments/{commentId}
     * 教师可以修改任何评论，学生只能修改自己的
     */
    @PutMapping("/{discussionId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            HttpServletRequest request,
            @PathVariable Long discussionId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest updateRequest) {
        User currentUser = requireLogin(request);
        
        // 获取评论信息以检查权限
        CommentResponse existingComment = discussionService.getCommentById(commentId);
        
        // 检查权限：教师/管理员可以修改任何，学生只能修改自己的
        if (!isTeacherOrAdmin(currentUser) && !currentUser.getId().equals(existingComment.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限修改此评论");
        }

        CommentResponse response = discussionService.updateComment(commentId, updateRequest, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 删除评论（软删除）
     * DELETE /api/discussions/{discussionId}/comments/{commentId}
     * 教师可以删除任何评论，学生只能删除自己的
     */
    @DeleteMapping("/{discussionId}/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            HttpServletRequest request,
            @PathVariable Long discussionId,
            @PathVariable Long commentId) {
        User currentUser = requireLogin(request);
        
        // 获取评论信息以检查权限
        CommentResponse existingComment = discussionService.getCommentById(commentId);
        
        // 检查权限：教师/管理员可以删除任何，学生只能删除自己的
        if (!isTeacherOrAdmin(currentUser) && !currentUser.getId().equals(existingComment.getAuthorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限删除此评论");
        }

        discussionService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    /**
     * 教师端：获取所有讨论帖（包括已删除的）
     * GET /api/discussions/admin/all?courseId=1
     * courseId 为必填参数
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<DiscussionResponse>> getAllDiscussionsForAdmin(
            HttpServletRequest request,
            @RequestParam(required = true) Long courseId) {
        User currentUser = requireLogin(request);
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师或管理员可以访问此接口");
        }

        List<DiscussionResponse> discussions = discussionService.getDiscussions(
                courseId,
                true,
                null,
                null,
                1,
                100
        );
        return ResponseEntity.ok(discussions);
    }

    private void applyDiscussionPermissions(DiscussionResponse resp, User currentUser) {
        boolean isTeacher = isTeacherOrAdmin(currentUser);
        boolean isOwner = currentUser != null && currentUser.getId() != null && currentUser.getId().equals(resp.getAuthorId());
        resp.setCanEdit(isTeacher);
        resp.setCanDelete(isTeacher || isOwner);
        boolean allowComment = resp.getAllowComment() == null || resp.getAllowComment() == 1;
        boolean isClosed = "CLOSED".equalsIgnoreCase(resp.getStatus()) || !allowComment;
        boolean teacherCanReply = isTeacher;
        resp.setCanReply(isClosed ? teacherCanReply : true);
        resp.setCanToggleComment(isTeacher);
    }

    private void applyCommentPermissions(List<CommentResponse> comments, User currentUser) {
        boolean isTeacher = isTeacherOrAdmin(currentUser);
        Long uid = currentUser != null ? currentUser.getId() : null;
        for (CommentResponse c : comments) {
            boolean isOwner = uid != null && uid.equals(c.getAuthorId());
            c.setCanDelete(isTeacher || isOwner);
            c.setCanEdit(isTeacher || isOwner);
            if (c.getReplies() != null && !c.getReplies().isEmpty()) {
                applyCommentPermissions(c.getReplies(), currentUser);
            }
        }
    }
}

