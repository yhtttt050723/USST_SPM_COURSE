package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentIdAndDeleted(Long assignmentId, Integer deleted);
    
    Optional<Submission> findByAssignmentIdAndStudentIdAndDeleted(Long assignmentId, Long studentId, Integer deleted);
    
    List<Submission> findByStudentIdAndDeleted(Long studentId, Integer deleted);
}

