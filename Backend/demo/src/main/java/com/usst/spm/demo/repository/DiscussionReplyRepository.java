package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.DiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {
    List<DiscussionReply> findByDiscussionIdAndDeletedOrderByCreatedAtAsc(Long discussionId, Integer deleted);
    
    List<DiscussionReply> findByParentReplyIdAndDeletedOrderByCreatedAtAsc(Long parentReplyId, Integer deleted);
    
    Optional<DiscussionReply> findByIdAndDeleted(Long id, Integer deleted);
    
    long countByDiscussionIdAndDeleted(Long discussionId, Integer deleted);
}

