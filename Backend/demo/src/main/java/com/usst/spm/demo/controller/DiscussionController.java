package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.service.DiscussionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
@CrossOrigin(origins = "*")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    /**
     * 创建讨论主题
     * POST /api/discussions
     * Body: { "courseId": 1, "title": "...", "content": "...", "authorId": 1 }
     */
    @PostMapping
    public ResponseEntity<DiscussionResponse> createDiscussion(@RequestBody Map<String, Object> request) {
        Long authorId = null;
        if (request.containsKey("authorId")) {
            authorId = Long.valueOf(request.get("authorId").toString());
        }
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少作者ID");
        }

        DiscussionCreateRequest createRequest = new DiscussionCreateRequest();
        if (request.containsKey("courseId")) {
            createRequest.setCourseId(Long.valueOf(request.get("courseId").toString()));
        }
        if (request.containsKey("title")) {
            createRequest.setTitle(request.get("title").toString());
        }
        if (request.containsKey("content")) {
            createRequest.setContent(request.get("content").toString());
        }

        DiscussionResponse response = discussionService.createDiscussion(authorId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取讨论列表
     * GET /api/discussions?courseId=1
     */
    @GetMapping
    public ResponseEntity<List<DiscussionResponse>> getDiscussions(
            @RequestParam(required = false) Long courseId) {
        List<DiscussionResponse> discussions = discussionService.getDiscussions(courseId);
        return ResponseEntity.ok(discussions);
    }

    /**
     * 获取讨论详情
     * GET /api/discussions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> getDiscussionById(@PathVariable Long id) {
        DiscussionResponse discussion = discussionService.getDiscussionById(id);
        return ResponseEntity.ok(discussion);
    }

    /**
     * 更新讨论
     * PUT /api/discussions/{id}
     * Body: { "title": "...", "content": "...", "isPinned": false, "isLocked": false, "authorId": 1 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionResponse> updateDiscussion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long authorId = null;
        if (request.containsKey("authorId")) {
            authorId = Long.valueOf(request.get("authorId").toString());
        }
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少作者ID");
        }

        DiscussionUpdateRequest updateRequest = new DiscussionUpdateRequest();
        if (request.containsKey("title")) {
            updateRequest.setTitle(request.get("title").toString());
        }
        if (request.containsKey("content")) {
            updateRequest.setContent(request.get("content").toString());
        }
        if (request.containsKey("isPinned")) {
            updateRequest.setIsPinned(Boolean.valueOf(request.get("isPinned").toString()));
        }
        if (request.containsKey("isLocked")) {
            updateRequest.setIsLocked(Boolean.valueOf(request.get("isLocked").toString()));
        }

        DiscussionResponse response = discussionService.updateDiscussion(id, authorId, updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除讨论
     * DELETE /api/discussions/{id}?authorId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscussion(
            @PathVariable Long id,
            @RequestParam Long authorId) {
        discussionService.deleteDiscussion(id, authorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 创建回复
     * POST /api/discussions/{discussionId}/replies
     * Body: { "content": "...", "parentReplyId": null, "authorId": 1 }
     */
    @PostMapping("/{discussionId}/replies")
    public ResponseEntity<DiscussionReplyResponse> createReply(
            @PathVariable Long discussionId,
            @RequestBody Map<String, Object> request) {
        Long authorId = null;
        if (request.containsKey("authorId")) {
            authorId = Long.valueOf(request.get("authorId").toString());
        }
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少作者ID");
        }

        DiscussionReplyCreateRequest createRequest = new DiscussionReplyCreateRequest();
        if (request.containsKey("content")) {
            createRequest.setContent(request.get("content").toString());
        }
        if (request.containsKey("parentReplyId") && request.get("parentReplyId") != null) {
            createRequest.setParentReplyId(Long.valueOf(request.get("parentReplyId").toString()));
        }

        DiscussionReplyResponse response = discussionService.createReply(discussionId, authorId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取讨论的所有回复
     * GET /api/discussions/{discussionId}/replies
     */
    @GetMapping("/{discussionId}/replies")
    public ResponseEntity<List<DiscussionReplyResponse>> getReplies(@PathVariable Long discussionId) {
        List<DiscussionReplyResponse> replies = discussionService.getRepliesByDiscussionId(discussionId);
        return ResponseEntity.ok(replies);
    }

    /**
     * 更新回复
     * PUT /api/discussions/replies/{replyId}
     * Body: { "content": "...", "authorId": 1 }
     */
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<DiscussionReplyResponse> updateReply(
            @PathVariable Long replyId,
            @RequestBody Map<String, Object> request) {
        Long authorId = null;
        if (request.containsKey("authorId")) {
            authorId = Long.valueOf(request.get("authorId").toString());
        }
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少作者ID");
        }

        DiscussionReplyUpdateRequest updateRequest = new DiscussionReplyUpdateRequest();
        if (request.containsKey("content")) {
            updateRequest.setContent(request.get("content").toString());
        }

        DiscussionReplyResponse response = discussionService.updateReply(replyId, authorId, updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除回复
     * DELETE /api/discussions/replies/{replyId}?authorId=1
     */
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long replyId,
            @RequestParam Long authorId) {
        discussionService.deleteReply(replyId, authorId);
        return ResponseEntity.noContent().build();
    }
}

