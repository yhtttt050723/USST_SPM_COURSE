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
import java.util.ArrayList;
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
        // 如果 courseId 为 null，使用默认课程ID（向后兼容）
        // courseId=0 表示全校公告
        Long targetCourseId = request.getCourseId() != null ? request.getCourseId() : DEFAULT_COURSE_ID;
        announcement.setCourseId(targetCourseId);
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
     * @param courseId 课程ID
     *                  - null: 未指定，向后兼容时返回默认课程公告
     *                  - 0: 全校公告
     *                  - > 0: 指定课程的公告
     * @param includeGlobal 是否包含全校公告
     *                      - true: 如果 courseId > 0，同时返回该课程公告和全校公告；如果 courseId 为 null/0，只返回全校公告
     *                      - false: 只返回指定 courseId 的公告（courseId=0时返回全校公告，courseId>0时返回课程公告，courseId=null时返回默认课程公告）
     */
    public List<AnnouncementResponse> getAnnouncements(Long courseId, boolean includeGlobal) {
        List<Announcement> announcements = new ArrayList<>();
        
        if (courseId != null && courseId == 0) {
            // courseId = 0: 只获取全校公告
            List<Announcement> globalAnnouncements = announcementRepository
                    .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(0L, 0);
            announcements.addAll(globalAnnouncements);
        } else if (courseId != null && courseId > 0) {
            // courseId > 0: 获取该课程的公告
            List<Announcement> courseAnnouncements = announcementRepository
                    .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(courseId, 0);
            announcements.addAll(courseAnnouncements);
            
            // 如果需要包含全校公告，同时获取
            if (includeGlobal) {
                List<Announcement> globalAnnouncements = announcementRepository
                        .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(0L, 0);
                announcements.addAll(globalAnnouncements);
            }
        } else {
            // courseId 为 null 或 <= 0: 向后兼容
            if (includeGlobal) {
                // 包含全校公告：返回默认课程公告 + 全校公告
                List<Announcement> defaultCourseAnnouncements = announcementRepository
                        .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(DEFAULT_COURSE_ID, 0);
                announcements.addAll(defaultCourseAnnouncements);
                List<Announcement> globalAnnouncements = announcementRepository
                        .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(0L, 0);
                announcements.addAll(globalAnnouncements);
            } else {
                // 不包含全校公告：只返回默认课程公告（向后兼容）
                List<Announcement> defaultCourseAnnouncements = announcementRepository
                        .findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(DEFAULT_COURSE_ID, 0);
                announcements.addAll(defaultCourseAnnouncements);
            }
        }
        
        // 去重（避免重复）并排序
        List<Announcement> distinctAnnouncements = announcements.stream()
                .collect(Collectors.toMap(Announcement::getId, a -> a, (a, b) -> a))
                .values()
                .stream()
                .sorted((a, b) -> {
                    // 先按置顶排序
                    int pinCompare = Boolean.compare(
                            b.getIsPinned() != null && b.getIsPinned(),
                            a.getIsPinned() != null && a.getIsPinned()
                    );
                    if (pinCompare != 0) return pinCompare;
                    // 再按创建时间倒序
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .collect(Collectors.toList());
        
        return distinctAnnouncements.stream()
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

