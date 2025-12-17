package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * 查找作业的提交，deleted 为空或为 0 均视为有效
     */
    @Query("SELECT s FROM Submission s WHERE s.assignmentId = :assignmentId AND (s.deleted = 0 OR s.deleted IS NULL)")
    List<Submission> findActiveByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 查找作业下某学生的提交，deleted 为空或为 0 均视为有效
     */
    @Query("SELECT s FROM Submission s WHERE s.assignmentId = :assignmentId AND s.studentId = :studentId AND (s.deleted = 0 OR s.deleted IS NULL)")
    Optional<Submission> findActiveByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId,
                                                              @Param("studentId") Long studentId);

    /**
     * 查找学生的提交，deleted 为空或为 0 均视为有效
     */
    @Query("SELECT s FROM Submission s WHERE s.studentId = :studentId AND (s.deleted = 0 OR s.deleted IS NULL)")
    List<Submission> findActiveByStudentId(@Param("studentId") Long studentId);
}

