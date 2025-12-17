package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.GradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 成绩历史Repository
 */
public interface GradeHistoryRepository extends JpaRepository<GradeHistory, Long> {
    
    /**
     * 根据成绩ID查询历史记录（按变更时间倒序）
     */
    List<GradeHistory> findByGradeIdAndDeletedOrderByChangedAtDesc(Long gradeId, Integer deleted);
    
    /**
     * 根据提交ID查询历史记录（按变更时间倒序）
     */
    List<GradeHistory> findBySubmissionIdAndDeletedOrderByChangedAtDesc(Long submissionId, Integer deleted);
}

