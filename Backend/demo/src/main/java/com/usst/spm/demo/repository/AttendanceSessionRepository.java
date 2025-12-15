package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.AttendanceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    Page<AttendanceSession> findByCourseIdOrderByStartTimeDesc(Long courseId, Pageable pageable);

    Optional<AttendanceSession> findFirstByCourseIdAndCodeAndStatusAndEndTimeAfter(
            Long courseId, String code, String status, LocalDateTime now);
}

