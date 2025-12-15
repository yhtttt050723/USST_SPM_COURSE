package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Page<AttendanceRecord> findBySessionIdOrderByCheckinAtDesc(Long sessionId, Pageable pageable);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    long countBySessionId(Long sessionId);

    Page<AttendanceRecord> findByStudentIdOrderByCheckinAtDesc(Long studentId, Pageable pageable);
}

