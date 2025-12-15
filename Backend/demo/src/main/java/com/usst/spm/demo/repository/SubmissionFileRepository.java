package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {

    /**
     * deleted 为 0 或 null 视为有效
     */
    @Query("SELECT sf FROM SubmissionFile sf WHERE sf.submissionId = :submissionId AND (sf.deleted = 0 OR sf.deleted IS NULL)")
    List<SubmissionFile> findActiveBySubmissionId(@Param("submissionId") Long submissionId);

    List<SubmissionFile> findBySubmissionIdAndDeleted(Long submissionId, Integer deleted);
}

