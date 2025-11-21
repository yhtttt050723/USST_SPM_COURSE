package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdAndDeleted(Long courseId, Integer deleted);
}

