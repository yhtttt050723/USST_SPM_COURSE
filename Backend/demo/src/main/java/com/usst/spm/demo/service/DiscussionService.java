package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.Comment;
import com.usst.spm.demo.model.Discussion;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.CommentRepository;
import com.usst.spm.demo.repository.DiscussionRepository;
import com.usst.spm.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscussionService {

    private static final Long DEFAULT_COURSE_ID = 1L;

    private final DiscussionRepository discussionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public DiscussionService(
            DiscussionRepository discussionRepository,
            CommentRepository commentRepository,
            UserRepository userRepository) {
        this.discussionRepository = discussionRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建讨论帖
     */
    @Transactional
    public DiscussionResponse createDiscussion(Long authorId, String authorRole, DiscussionCreateRequest request) {
        Discussion discussion = new Discussion();
        discussion.setCourseId(request.getCourseId() != null ? request.getCourseId() : DEFAULT_COURSE_ID);
        discussion.setAuthorId(authorId);
        discussion.setAuthorRole(authorRole);
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setCreatedAt(LocalDateTime.now());
        discussion.setUpdatedAt(LocalDateTime.now());
        discussion.setCreatedBy(authorId);
        discussion.setUpdatedBy(authorId);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            discussion.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getPinned() != null) {
            discussion.setPinned(Boolean.TRUE.equals(request.getPinned()) ? 1 : 0);
        }
        if (request.getAllowComment() != null) {
            discussion.setAllowComment(Boolean.TRUE.equals(request.getAllowComment()) ? 1 : 0);
        } else if ("CLOSED".equalsIgnoreCase(discussion.getStatus())) {
            discussion.setAllowComment(0);
        }

        Discussion saved = discussionRepository.save(discussion);
        return convertToResponse(saved, true);
    }

    /**
     * 获取讨论帖列表（教师可以看到所有，学生只能看到未删除的）
     */
    public List<DiscussionResponse> getDiscussions(Long courseId, boolean includeDeleted, String keyword, String status, int page, int size) {
        List<Discussion> discussions;
        Long targetCourseId = courseId != null ? courseId : DEFAULT_COURSE_ID;
        
        if (includeDeleted) {
            // 教师端：可以看到所有（包括已删除的）
            // 获取该课程的所有讨论帖（包括已删除的）
            discussions = discussionRepository.findAll().stream()
                    .filter(d -> targetCourseId.equals(d.getCourseId()))
                    .collect(Collectors.toList());
        } else {
            // 学生端：只能看到未删除的
            discussions = discussionRepository.findByCourseIdAndDeleted(targetCourseId, 0);
        }

        // 按置顶和时间排序（lastReplyAt 优先，其次 createdAt）
        discussions.sort((a, b) -> {
            int pinCompare = Integer.compare(
                    b.getPinned() != null ? b.getPinned() : 0,
                    a.getPinned() != null ? a.getPinned() : 0
            );
            if (pinCompare != 0) return pinCompare;
            LocalDateTime left = b.getLastReplyAt() != null ? b.getLastReplyAt() : b.getCreatedAt();
            LocalDateTime right = a.getLastReplyAt() != null ? a.getLastReplyAt() : a.getCreatedAt();
            if (left == null && right == null) {
                return 0;
            }
            if (left == null) {
                return 1;
            }
            if (right == null) {
                return -1;
            }
            return left.compareTo(right);
        });

        // 关键词过滤
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            discussions = discussions.stream()
                    .filter(d -> (d.getTitle() != null && d.getTitle().toLowerCase().contains(kw))
                            || (d.getContent() != null && d.getContent().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            String st = status.toUpperCase();
            discussions = discussions.stream()
                    .filter(d -> st.equalsIgnoreCase(d.getStatus()))
                    .collect(Collectors.toList());
        }

        // 分页（简单截取，page从1开始）
        int pageIndex = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= discussions.size()) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + pageSize, discussions.size());
        List<Discussion> pageList = discussions.subList(fromIndex, toIndex);

        return pageList.stream()
                .map(d -> convertToResponse(d, false))
                .collect(Collectors.toList());
    }

    /**
     * 获取讨论帖详情（包含评论）
     */
    public DiscussionResponse getDiscussionById(Long id, boolean includeDeleted) {
        Optional<Discussion> discussionOpt;
        if (includeDeleted) {
            discussionOpt = discussionRepository.findById(id);
        } else {
            discussionOpt = discussionRepository.findByIdAndDeleted(id, 0);
        }

        if (discussionOpt.isEmpty()) {
            throw new RuntimeException("讨论帖不存在");
        }

        return convertToResponse(discussionOpt.get(), true);
    }

    /**
     * 更新讨论帖
     */
    @Transactional
    public DiscussionResponse updateDiscussion(Long id, DiscussionUpdateRequest request, Long operatorId) {
        Optional<Discussion> discussionOpt = discussionRepository.findById(id);
        if (discussionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论帖不存在");
        }

        Discussion discussion = discussionOpt.get();
        if (request.getTitle() != null) {
            discussion.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            discussion.setContent(request.getContent());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            discussion.setStatus(request.getStatus().toUpperCase());
            if ("CLOSED".equalsIgnoreCase(discussion.getStatus())) {
                discussion.setAllowComment(0);
            } else if ("OPEN".equalsIgnoreCase(discussion.getStatus())) {
                discussion.setAllowComment(1);
            }
        }
        if (request.getAllowComment() != null) {
            discussion.setAllowComment(Boolean.TRUE.equals(request.getAllowComment()) ? 1 : 0);
        }
        discussion.setUpdatedAt(LocalDateTime.now());
        discussion.setUpdatedBy(operatorId);

        Discussion saved = discussionRepository.save(discussion);
        return convertToResponse(saved, true);
    }

    /**
     * 删除讨论帖（软删除）
     */
    @Transactional
    public void deleteDiscussion(Long id, Long operatorId) {
        Optional<Discussion> discussionOpt = discussionRepository.findById(id);
        if (discussionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论帖不存在");
        }

        Discussion discussion = discussionOpt.get();
        discussion.setDeleted(1);
        discussion.setReplyCount(0);
        discussion.setLastReplyAt(null);
        discussion.setUpdatedAt(LocalDateTime.now());
        discussion.setDeletedAt(LocalDateTime.now());
        discussion.setDeletedBy(operatorId);
        discussion.setUpdatedBy(operatorId);
        discussionRepository.save(discussion);

        // 同时删除所有评论
        List<Comment> comments = commentRepository.findByDiscussionIdAndDeleted(id, 0);
        for (Comment comment : comments) {
            comment.setDeleted(1);
            comment.setUpdatedAt(LocalDateTime.now());
            comment.setDeletedAt(LocalDateTime.now());
            comment.setDeletedBy(operatorId);
            comment.setUpdatedBy(operatorId);
            commentRepository.save(comment);
        }
    }

    /**
     * 创建评论
     */
    @Transactional
    public CommentResponse createComment(Long discussionId, Long authorId, String authorRole, CommentCreateRequest request) {
        // 验证讨论帖存在
        Discussion discussion = discussionRepository.findByIdAndDeleted(discussionId, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论帖不存在"));
        boolean isTeacherOrAdmin = "TEACHER".equalsIgnoreCase(authorRole) || "ADMIN".equalsIgnoreCase(authorRole);
        if (discussion.getAllowComment() != null && discussion.getAllowComment() == 0 && !isTeacherOrAdmin) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该讨论已关闭评论");
        }

        Comment comment = new Comment();
        comment.setDiscussionId(discussionId);
        comment.setAuthorId(authorId);
        comment.setAuthorRole(authorRole);
        comment.setContent(request.getContent());
        if (request.getParentId() != null) {
            // 验证父评论存在
            Optional<Comment> parentOpt = commentRepository.findByIdAndDeleted(request.getParentId(), 0);
            if (parentOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "父评论不存在");
            }
            comment.setParentId(request.getParentId());
        }
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setCreatedBy(authorId);
        comment.setUpdatedBy(authorId);

        Comment saved = commentRepository.save(comment);
        refreshReplyStats(discussionId, authorId);
        return convertCommentToResponse(saved);
    }

    /**
     * 更新评论
     */
    @Transactional
    public CommentResponse updateComment(Long id, CommentUpdateRequest request, Long operatorId) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在");
        }

        Comment comment = commentOpt.get();
        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setUpdatedBy(operatorId);

        Comment saved = commentRepository.save(comment);
        return convertCommentToResponse(saved);
    }

    /**
     * 获取单个评论
     */
    public CommentResponse getCommentById(Long id) {
        return commentRepository.findByIdAndDeleted(id, 0)
                .map(this::convertCommentToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在"));
    }

    /**
     * 删除评论（软删除）
     */
    @Transactional
    public void deleteComment(Long id, Long operatorId) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在");
        }

        Comment comment = commentOpt.get();
        comment.setDeleted(1);
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setDeletedAt(LocalDateTime.now());
        comment.setDeletedBy(operatorId);
        comment.setUpdatedBy(operatorId);
        commentRepository.save(comment);

        // 同时删除所有子评论
        List<Comment> replies = commentRepository.findByParentIdAndDeleted(id, 0);
        for (Comment reply : replies) {
            reply.setDeleted(1);
            reply.setUpdatedAt(LocalDateTime.now());
            reply.setDeletedAt(LocalDateTime.now());
            reply.setDeletedBy(operatorId);
            reply.setUpdatedBy(operatorId);
            commentRepository.save(reply);
        }

        refreshReplyStats(comment.getDiscussionId(), operatorId);
    }

    /**
     * 置顶/取消置顶讨论帖
     */
    @Transactional
    public DiscussionResponse pinDiscussion(Long id, boolean pin, Long operatorId) {
        Optional<Discussion> discussionOpt = discussionRepository.findById(id);
        if (discussionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论帖不存在");
        }

        Discussion discussion = discussionOpt.get();
        discussion.setPinned(pin ? 1 : 0);
        discussion.setUpdatedAt(LocalDateTime.now());
        discussion.setUpdatedBy(operatorId);

        Discussion saved = discussionRepository.save(discussion);
        return convertToResponse(saved, true);
    }

    /**
     * 开启/关闭评论
     */
    @Transactional
    public DiscussionResponse switchComment(Long id, boolean allow, Long operatorId) {
        Optional<Discussion> discussionOpt = discussionRepository.findById(id);
        if (discussionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论帖不存在");
        }
        Discussion discussion = discussionOpt.get();
        discussion.setAllowComment(allow ? 1 : 0);
        discussion.setStatus(allow ? "OPEN" : "CLOSED");
        discussion.setUpdatedAt(LocalDateTime.now());
        discussion.setUpdatedBy(operatorId);
        Discussion saved = discussionRepository.save(discussion);
        return convertToResponse(saved, true);
    }

    /**
     * 转换为响应DTO
     */
    private DiscussionResponse convertToResponse(Discussion discussion, boolean includeComments) {
        DiscussionResponse response = new DiscussionResponse();
        response.setId(discussion.getId());
        response.setCourseId(discussion.getCourseId());
        response.setAuthorId(discussion.getAuthorId());
        response.setAuthorRole(discussion.getAuthorRole());
        response.setTitle(discussion.getTitle());
        response.setContent(discussion.getContent());
        response.setStatus(discussion.getStatus());
        response.setPin(discussion.getPinned());
        response.setAllowComment(discussion.getAllowComment());
        response.setReplyCount(discussion.getReplyCount());
        response.setLastReplyAt(discussion.getLastReplyAt());
        response.setDeleted(discussion.getDeleted());
        response.setDeletedAt(discussion.getDeletedAt());
        response.setCreatedAt(discussion.getCreatedAt());
        response.setUpdatedAt(discussion.getUpdatedAt());

        // 获取作者信息
        Optional<User> authorOpt = userRepository.findById(discussion.getAuthorId());
        if (authorOpt.isPresent()) {
            User author = authorOpt.get();
            response.setAuthorName(author.getName());
            response.setAuthorStudentNo(author.getStudentNo());
        }

        // 获取评论
        if (includeComments) {
            List<Comment> comments = commentRepository.findByDiscussionIdAndDeleted(discussion.getId(), 0);
            List<CommentResponse> commentResponses = buildCommentTree(comments);
            response.setComments(commentResponses);
            response.setCommentCount(comments.size());
        } else {
            // 只统计评论数量
            List<Comment> comments = commentRepository.findByDiscussionIdAndDeleted(discussion.getId(), 0);
            response.setCommentCount(comments.size());
        }

        return response;
    }

    public List<CommentResponse> getCommentsPaged(Long discussionId, boolean includeDeleted, int page, int size) {
        List<Comment> comments = commentRepository.findAll().stream()
                .filter(c -> discussionId.equals(c.getDiscussionId()))
                .filter(c -> includeDeleted || c.getDeleted() == null || c.getDeleted() == 0)
                .collect(Collectors.toList());
        comments.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return a.getCreatedAt().compareTo(b.getCreatedAt());
        });
        int pageIndex = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= comments.size()) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + pageSize, comments.size());
        List<Comment> pageList = comments.subList(fromIndex, toIndex);
        return pageList.stream().map(this::convertCommentToResponse).collect(Collectors.toList());
    }

    /**
     * 构建评论树结构
     */
    private List<CommentResponse> buildCommentTree(List<Comment> comments) {
        // 找出所有顶级评论（parentId为null）
        List<Comment> topLevelComments = comments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        List<CommentResponse> result = new ArrayList<>();
        for (Comment comment : topLevelComments) {
            CommentResponse response = convertCommentToResponse(comment);
            // 查找子评论
            List<CommentResponse> replies = comments.stream()
                    .filter(c -> comment.getId().equals(c.getParentId()))
                    .map(this::convertCommentToResponse)
                    .collect(Collectors.toList());
            response.setReplies(replies);
            result.add(response);
        }

        return result;
    }

    private void refreshReplyStats(Long discussionId, Long operatorId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论帖不存在"));
        List<Comment> activeComments = commentRepository.findByDiscussionIdAndDeleted(discussionId, 0);
        discussion.setReplyCount(activeComments.size());
        LocalDateTime last = activeComments.stream()
                .map(Comment::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        discussion.setLastReplyAt(last);
        discussion.setUpdatedAt(LocalDateTime.now());
        if (operatorId != null) {
            discussion.setUpdatedBy(operatorId);
        }
        discussionRepository.save(discussion);
    }

    /**
     * 转换评论为响应DTO
     */
    private CommentResponse convertCommentToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setDiscussionId(comment.getDiscussionId());
        response.setParentId(comment.getParentId());
        response.setAuthorId(comment.getAuthorId());
        response.setAuthorRole(comment.getAuthorRole());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setDeleted(comment.getDeleted());

        // 获取作者信息
        Optional<User> authorOpt = userRepository.findById(comment.getAuthorId());
        if (authorOpt.isPresent()) {
            User author = authorOpt.get();
            response.setAuthorName(author.getName());
            response.setAuthorStudentNo(author.getStudentNo());
        }

        return response;
    }
}

