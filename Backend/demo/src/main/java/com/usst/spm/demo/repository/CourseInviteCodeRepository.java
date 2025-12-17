package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.CourseInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseInviteCodeRepository extends JpaRepository<CourseInviteCode, Long> {
    Optional<CourseInviteCode> findByCodeAndActive(String code, Integer active);
    List<CourseInviteCode> findByCourseIdAndActive(Long courseId, Integer active);
}

