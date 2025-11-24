package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findBySubmissionIdAndDeleted(Long submissionId, Integer deleted);
    
    List<Grade> findBySubmissionIdInAndDeleted(List<Long> submissionIds, Integer deleted);
}

