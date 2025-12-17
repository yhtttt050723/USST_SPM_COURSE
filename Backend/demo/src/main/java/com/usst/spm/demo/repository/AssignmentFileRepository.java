package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 作业附件Repository
 */
public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, Long> {
    
    /**
     * 根据作业ID查询附件列表
     */
    List<AssignmentFile> findByAssignmentIdAndDeleted(Long assignmentId, Integer deleted);
}

