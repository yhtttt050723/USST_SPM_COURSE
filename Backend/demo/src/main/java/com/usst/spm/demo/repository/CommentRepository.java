package com.usst.spm.demo.repository;

import com.usst.spm.demo.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDiscussionIdAndDeleted(Long discussionId, Integer deleted);
    List<Comment> findByDiscussionIdAndParentIdAndDeleted(Long discussionId, Long parentId, Integer deleted);
    Optional<Comment> findByIdAndDeleted(Long id, Integer deleted);
    List<Comment> findByParentIdAndDeleted(Long parentId, Integer deleted);
}

