package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.*;
import com.usst.spm.demo.model.Announcement;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.AnnouncementRepository;
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
public class AnnouncementService {

    private static final Long DEFAULT_COURSE_ID = 1L;

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建公告
     */
    @Transactional
    public AnnouncementResponse createAnnouncement(Long authorId, AnnouncementCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
        }

        Announcement announcement = new Announcement();
        announcement.setCourseId(request.getCourseId() != null ? request.getCourseId() : DEFAULT_COURSE_ID);
        announcement.setAuthorId(authorId);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        if (request.getIsPinned() != null) {
            announcement.setIsPinned(request.getIsPinned());
        }
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());

        Announcement saved = announcementRepository.save(announcement);
        return convertToResponse(saved);
    }

    /**
     * 获取公告列表
     */
    public List<AnnouncementResponse> getAnnouncements(Long courseId) {
        Long targetCourseId = courseId != null ? courseId : DEFAULT_COURSE_ID;
        List<Announcement> announcements = announcementRepository
                .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(targetCourseId, 0);
        return announcements.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取公告详情
     */
    public AnnouncementResponse getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));
        return convertToResponse(announcement);
    }

    /**
     * 更新公告
     */
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, Long authorId, AnnouncementUpdateRequest request) {
        Announcement announcement = announcementRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));

        // 仅作者可修改
        if (!announcement.getAuthorId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权修改此公告");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            announcement.setContent(request.getContent());
        }
        if (request.getIsPinned() != null) {
            announcement.setIsPinned(request.getIsPinned());
        }
        announcement.setUpdatedAt(LocalDateTime.now());

        Announcement saved = announcementRepository.save(announcement);
        return convertToResponse(saved);
    }

    /**
     * 删除公告（软删除）
     */
    @Transactional
    public void deleteAnnouncement(Long id, Long authorId) {
        Announcement announcement = announcementRepository.findByIdAndDeleted(id, 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));

        if (!announcement.getAuthorId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权删除此公告");
        }

        announcement.setDeleted(1);
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);
    }

    /**
     * 转换 Announcement 为 AnnouncementResponse
     */
    private AnnouncementResponse convertToResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setCourseId(announcement.getCourseId());
        response.setAuthorId(announcement.getAuthorId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setIsPinned(announcement.getIsPinned() != null ? announcement.getIsPinned() : false);
        response.setCreatedAt(announcement.getCreatedAt());
        response.setUpdatedAt(announcement.getUpdatedAt());

        Optional<User> authorOpt = userRepository.findById(announcement.getAuthorId());
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

