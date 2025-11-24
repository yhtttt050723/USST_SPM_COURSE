package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {
    List<SubmissionFile> findBySubmissionIdAndDeleted(Long submissionId, Integer deleted);
}

