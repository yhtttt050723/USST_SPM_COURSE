package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.Discussion;
import com.usst.spm.demo.model.DiscussionReply;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.DiscussionRepository;
import com.usst.spm.demo.repository.DiscussionReplyRepository;
import com.usst.spm.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscussionService {

    private static final Long DEFAULT_COURSE_ID = 1L;

    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository discussionReplyRepository;
    private final UserRepository userRepository;

    public DiscussionService(
            DiscussionRepository discussionRepository,
            DiscussionReplyRepository discussionReplyRepository,
            UserRepository userRepository) {
        this.discussionRepository = discussionRepository;
        this.discussionReplyRepository = discussionReplyRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建讨论主题
     */
    @Transactional
    public DiscussionResponse createDiscussion(Long authorId, DiscussionCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
        }

        Discussion discussion = new Discussion();
        discussion.setCourseId(request.getCourseId() != null ? request.getCourseId() : DEFAULT_COURSE_ID);
        discussion.setAuthorId(authorId);
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setCreatedAt(LocalDateTime.now());
        discussion.setUpdatedAt(LocalDateTime.now());

        Discussion saved = discussionRepository.save(discussion);
        return convertToResponse(saved);
    }

    /**
     * 获取讨论列表
     */
    public List<DiscussionResponse> getDiscussions(Long courseId) {
        Long targetCourseId = courseId != null ? courseId : DEFAULT_COURSE_ID;
        List<Discussion> discussions = discussionRepository
                .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(targetCourseId, 0);

        return discussions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取讨论详情（增加浏览量）
     */
    @Transactional
    public DiscussionResponse getDiscussionById(Long id) {
        Discussion discussion = discussionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论不存在"));

        // 增加浏览量
        discussion.setViewCount((discussion.getViewCount() != null ? discussion.getViewCount() : 0) + 1);
        discussionRepository.save(discussion);

        DiscussionResponse response = convertToResponse(discussion);
        
        // 回复列表通过单独的接口获取，不在这里加载
        
        return response;
    }

    /**
     * 更新讨论
     */
    @Transactional
    public DiscussionResponse updateDiscussion(Long id, Long authorId, DiscussionUpdateRequest request) {
        Discussion discussion = discussionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论不存在"));

        // 检查权限：只有作者可以修改
        if (!discussion.getAuthorId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权修改此讨论");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            discussion.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            discussion.setContent(request.getContent());
        }
        if (request.getIsPinned() != null) {
            discussion.setIsPinned(request.getIsPinned());
        }
        if (request.getIsLocked() != null) {
            discussion.setIsLocked(request.getIsLocked());
        }
        discussion.setUpdatedAt(LocalDateTime.now());

        Discussion saved = discussionRepository.save(discussion);
        return convertToResponse(saved);
    }

    /**
     * 删除讨论（软删除）
     */
    @Transactional
    public void deleteDiscussion(Long id, Long authorId) {
        Discussion discussion = discussionRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论不存在"));

        // 检查权限：只有作者可以删除
        if (!discussion.getAuthorId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除此讨论");
        }

        discussion.setDeleted(1);
        discussion.setUpdatedAt(LocalDateTime.now());
        discussionRepository.save(discussion);
    }

    /**
     * 创建回复
     */
    @Transactional
    public DiscussionReplyResponse createReply(Long discussionId, Long authorId, DiscussionReplyCreateRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "回复内容不能为空");
        }

        Discussion discussion = discussionRepository.findByIdAndDeleted(discussionId, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "讨论不存在"));

        // 检查讨论是否被锁定
        if (Boolean.TRUE.equals(discussion.getIsLocked())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "讨论已被锁定，无法回复");
        }

        DiscussionReply reply = new DiscussionReply();
        reply.setDiscussionId(discussionId);
        reply.setAuthorId(authorId);
        reply.setContent(request.getContent());
        reply.setParentReplyId(request.getParentReplyId());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());

        DiscussionReply saved = discussionReplyRepository.save(reply);

        // 更新讨论的回复数
        long replyCount = discussionReplyRepository.countByDiscussionIdAndDeleted(discussionId, 0);
        discussion.setReplyCount((int) replyCount);
        discussion.setUpdatedAt(LocalDateTime.now());
        discussionRepository.save(discussion);

        return convertToReplyResponse(saved);
    }

    /**
     * 获取讨论的所有回复
     */
    public List<DiscussionReplyResponse> getRepliesByDiscussionId(Long discussionId) {
        // 获取所有顶级回复（parentReplyId 为 null）
        List<DiscussionReply> topLevelReplies = discussionReplyRepository
                .findByDiscussionIdAndDeletedOrderByCreatedAtAsc(discussionId, 0)
                .stream()
                .filter(reply -> reply.getParentReplyId() == null)
                .collect(Collectors.toList());

        return topLevelReplies.stream()
                .map(reply -> {
                    DiscussionReplyResponse response = convertToReplyResponse(reply);
                    // 加载子回复
                    List<DiscussionReplyResponse> childReplies = getChildReplies(reply.getId());
                    response.setReplies(childReplies);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取子回复（递归）
     */
    private List<DiscussionReplyResponse> getChildReplies(Long parentReplyId) {
        List<DiscussionReply> childReplies = discussionReplyRepository
                .findByParentReplyIdAndDeletedOrderByCreatedAtAsc(parentReplyId, 0);

        return childReplies.stream()
                .map(reply -> {
                    DiscussionReplyResponse response = convertToReplyResponse(reply);
                    // 递归加载子回复
                    List<DiscussionReplyResponse> grandChildReplies = getChildReplies(reply.getId());
                    response.setReplies(grandChildReplies);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新回复
     */
    @Transactional
    public DiscussionReplyResponse updateReply(Long replyId, Long authorId, DiscussionReplyUpdateRequest request) {
        DiscussionReply reply = discussionReplyRepository.findByIdAndDeleted(replyId, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "回复不存在"));

        // 检查权限：只有作者可以修改
        if (!reply.getAuthorId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权修改此回复");
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            reply.setContent(request.getContent());
        }
        reply.setUpdatedAt(LocalDateTime.now());

        DiscussionReply saved = discussionReplyRepository.save(reply);
        return convertToReplyResponse(saved);
    }

    /**
     * 删除回复（软删除）
     */
    @Transactional
    public void deleteReply(Long replyId, Long authorId) {
        DiscussionReply reply = discussionReplyRepository.findByIdAndDeleted(replyId, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "回复不存在"));

        // 检查权限：只有作者可以删除
        if (!reply.getAuthorId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除此回复");
        }

        reply.setDeleted(1);
        reply.setUpdatedAt(LocalDateTime.now());
        discussionReplyRepository.save(reply);

        // 更新讨论的回复数
        Discussion discussion = discussionRepository.findByIdAndDeleted(reply.getDiscussionId(), 0)
                .orElse(null);
        if (discussion != null) {
            long replyCount = discussionReplyRepository.countByDiscussionIdAndDeleted(reply.getDiscussionId(), 0);
            discussion.setReplyCount((int) replyCount);
            discussion.setUpdatedAt(LocalDateTime.now());
            discussionRepository.save(discussion);
        }
    }

    /**
     * 转换 Discussion 为 DiscussionResponse
     */
    private DiscussionResponse convertToResponse(Discussion discussion) {
        DiscussionResponse response = new DiscussionResponse();
        response.setId(discussion.getId());
        response.setCourseId(discussion.getCourseId());
        response.setAuthorId(discussion.getAuthorId());
        response.setTitle(discussion.getTitle());
        response.setContent(discussion.getContent());
        response.setReplyCount(discussion.getReplyCount() != null ? discussion.getReplyCount() : 0);
        response.setViewCount(discussion.getViewCount() != null ? discussion.getViewCount() : 0);
        response.setIsPinned(discussion.getIsPinned() != null ? discussion.getIsPinned() : false);
        response.setIsLocked(discussion.getIsLocked() != null ? discussion.getIsLocked() : false);
        response.setCreatedAt(discussion.getCreatedAt());
        response.setUpdatedAt(discussion.getUpdatedAt());

        // 加载作者信息
        Optional<User> authorOpt = userRepository.findById(discussion.getAuthorId());
        if (authorOpt.isPresent()) {
            User author = authorOpt.get();
            response.setAuthorName(author.getName());
            response.setAuthorNo(author.getStudentNo());
        } else {
            response.setAuthorName("未知用户");
            response.setAuthorNo("");
        }

        return response;
    }

    /**
     * 转换 DiscussionReply 为 DiscussionReplyResponse
     */
    private DiscussionReplyResponse convertToReplyResponse(DiscussionReply reply) {
        DiscussionReplyResponse response = new DiscussionReplyResponse();
        response.setId(reply.getId());
        response.setDiscussionId(reply.getDiscussionId());
        response.setAuthorId(reply.getAuthorId());
        response.setParentReplyId(reply.getParentReplyId());
        response.setContent(reply.getContent());
        response.setCreatedAt(reply.getCreatedAt());
        response.setUpdatedAt(reply.getUpdatedAt());

        // 加载作者信息
        Optional<User> authorOpt = userRepository.findById(reply.getAuthorId());
        if (authorOpt.isPresent()) {
            User author = authorOpt.get();
            response.setAuthorName(author.getName());
            response.setAuthorNo(author.getStudentNo());
        } else {
            response.setAuthorName("未知用户");
            response.setAuthorNo("");
        }

        return response;
    }
}

