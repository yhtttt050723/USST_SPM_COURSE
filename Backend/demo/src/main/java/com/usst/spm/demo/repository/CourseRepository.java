package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherIdAndDeleted(Long teacherId, Integer deleted);
    Optional<Course> findByInviteCodeAndDeleted(String inviteCode, Integer deleted);
    Optional<Course> findByCode(String code);
    List<Course> findByIdIn(List<Long> ids);
}

