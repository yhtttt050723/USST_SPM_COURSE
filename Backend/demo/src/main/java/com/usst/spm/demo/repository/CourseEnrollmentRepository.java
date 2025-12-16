package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourseIdAndStudentIdAndDeleted(Long courseId, Long studentId, Integer deleted);
    List<CourseEnrollment> findByStudentIdAndStatusAndDeleted(Long studentId, String status, Integer deleted);
    List<CourseEnrollment> findByCourseIdAndDeleted(Long courseId, Integer deleted);
}

