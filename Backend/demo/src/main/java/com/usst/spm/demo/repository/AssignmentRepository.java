package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdAndDeleted(Long courseId, Integer deleted);
    
    /**
     * 根据origin_id查询同一作业链路的所有版本（按版本号排序）
     */
    List<Assignment> findByOriginIdAndDeletedOrderByVersionDesc(Long originId, Integer deleted);
    
    /**
     * 根据origin_id查询最大版本号
     */
    Optional<Assignment> findFirstByOriginIdAndDeletedOrderByVersionDesc(Long originId, Integer deleted);
    
    /**
     * 查询原始作业（origin_id为null或等于id的作业）
     */
    List<Assignment> findByOriginIdIsNullAndDeletedOrderByCreatedAtDesc(Integer deleted);
}

