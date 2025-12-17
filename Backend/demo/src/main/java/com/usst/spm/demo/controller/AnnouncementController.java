package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserRepository userRepository;

    public AnnouncementController(AnnouncementService announcementService, UserRepository userRepository) {
        this.announcementService = announcementService;
        this.userRepository = userRepository;
    }
    
    /**
     * 获取当前登录用户
     */
    private User requireLogin(HttpServletRequest request) {
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    }

    /**
     * 创建公告（仅教师可访问）
     * POST /api/announcements
     * Body: { "courseId": 1（或0表示全校公告）, "title": "...", "content": "...", "isPinned": false }
     */
    @PostMapping
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            HttpServletRequest httpRequest,
            @RequestBody AnnouncementCreateRequest createRequest) {
        User currentUser = requireLogin(httpRequest);
        
        // 只有教师可以创建公告
        if (!"TEACHER".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师可以创建公告");
        }
        
        if (createRequest.getTitle() == null || createRequest.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题不能为空");
        }
        if (createRequest.getContent() == null || createRequest.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
        }
        
        // courseId 为 null 时，默认使用 0（全校公告）
        if (createRequest.getCourseId() == null) {
            createRequest.setCourseId(0L);
        }

        AnnouncementResponse response = announcementService.createAnnouncement(currentUser.getId(), createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取公告列表
     * GET /api/announcements?courseId=1&includeGlobal=true
     * - courseId: 课程ID，如果为0或null，只返回全校公告
     * - includeGlobal: 是否包含全校公告（默认true），学生端查看时会同时显示当前课程公告和全校公告
     */
    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false, defaultValue = "true") boolean includeGlobal) {
        List<AnnouncementResponse> announcements = announcementService.getAnnouncements(courseId, includeGlobal);
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
     * 更新公告（仅教师可访问）
     * PUT /api/announcements/{id}
     * Body: { "title": "...", "content": "...", "isPinned": false }
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody AnnouncementUpdateRequest updateRequest) {
        User currentUser = requireLogin(httpRequest);
        
        // 只有教师可以更新公告
        if (!"TEACHER".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师可以更新公告");
        }
        
        AnnouncementResponse response = announcementService.updateAnnouncement(id, currentUser.getId(), updateRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除公告（仅教师可访问）
     * DELETE /api/announcements/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAnnouncement(
            HttpServletRequest httpRequest,
            @PathVariable Long id) {
        User currentUser = requireLogin(httpRequest);
        
        // 只有教师可以删除公告
        if (!"TEACHER".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅教师可以删除公告");
        }
        
        announcementService.deleteAnnouncement(id, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}

