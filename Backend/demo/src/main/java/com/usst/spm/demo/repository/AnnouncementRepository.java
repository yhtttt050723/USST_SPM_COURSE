package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(Long courseId, Integer deleted);

    Optional<Announcement> findByIdAndDeleted(Long id, Integer deleted);
}

