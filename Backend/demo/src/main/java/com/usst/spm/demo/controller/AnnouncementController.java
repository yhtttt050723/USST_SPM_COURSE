package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 创建公告
     * POST /api/announcements
     * Body: { "courseId": 1, "title": "...", "content": "...", "isPinned": false, "authorId": 1 }
     */
    @PostMapping
    public ResponseEntity<AnnouncementResponse> createAnnouncement(@RequestBody Map<String, Object> request) {
        Long authorId = null;
        if (request.containsKey("authorId")) {
            authorId = Long.valueOf(request.get("authorId").toString());
        }
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少作者ID");
        }

        AnnouncementCreateRequest createRequest = new AnnouncementCreateRequest();
        if (request.containsKey("courseId")) {
            createRequest.setCourseId(Long.valueOf(request.get("courseId").toString()));
        }
        if (request.containsKey("title")) {
            createRequest.setTitle(request.get("title").toString());
        }
        if (request.containsKey("content")) {
            createRequest.setContent(request.get("content").toString());
        }
        if (request.containsKey("isPinned")) {
            createRequest.setIsPinned(Boolean.valueOf(request.get("isPinned").toString()));
        }

        AnnouncementResponse response = announcementService.createAnnouncement(authorId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取公告列表
     * GET /api/announcements?courseId=1
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@RequestParam(required = false) Long courseId) {
        List<AnnouncementResponse> announcements = announcementService.getAnnouncements(courseId);
        return ResponseEntity.ok(announcements);
    }

    /**
     * 获取公告详情
     * GET /api/announcements/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncementById(@PathVariable Long id) {
        AnnouncementResponse response = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新公告
     * PUT /api/announcements/{id}
     * Body: { "title": "...", "content": "...", "isPinned": false, "authorId": 1 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Long authorId = null;
        if (request.containsKey("authorId")) {
            authorId = Long.valueOf(request.get("authorId").toString());
        }
        if (authorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少作者ID");
        }

        AnnouncementUpdateRequest updateRequest = new AnnouncementUpdateRequest();
        if (request.containsKey("title")) {
            updateRequest.setTitle(request.get("title").toString());
        }
        if (request.containsKey("content")) {
            updateRequest.setContent(request.get("content").toString());
        }
        if (request.containsKey("isPinned")) {
            updateRequest.setIsPinned(Boolean.valueOf(request.get("isPinned").toString()));
        }

        AnnouncementResponse response = announcementService.updateAnnouncement(id, authorId, updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除公告
     * DELETE /api/announcements/{id}?authorId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id, @RequestParam Long authorId) {
        announcementService.deleteAnnouncement(id, authorId);
        return ResponseEntity.noContent().build();
    }
}

