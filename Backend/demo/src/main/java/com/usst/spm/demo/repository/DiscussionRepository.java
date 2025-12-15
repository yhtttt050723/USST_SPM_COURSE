package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    List<Discussion> findByCourseIdAndDeletedOrderByIsPinnedDescCreatedAtDesc(Long courseId, Integer deleted);
    
    Optional<Discussion> findByIdAndDeleted(Long id, Integer deleted);
    
    List<Discussion> findByAuthorIdAndDeletedOrderByCreatedAtDesc(Long authorId, Integer deleted);
}

