package com.inkwell.comment.repository;

import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.Comment.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Optional<Comment> findByCommentId(Integer commentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.postId = :postId
              AND c.parentCommentId IS NULL
              AND c.status <> 'DELETED'
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findTopLevelByPostId(@Param("postId") Integer postId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.postId = :postId
              AND c.status <> 'DELETED'
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findVisibleByPostId(@Param("postId") Integer postId);

    List<Comment> findByPostIdOrderByCreatedAtAsc(Integer postId);

    List<Comment> findByParentCommentId(Integer parentCommentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.parentCommentId = :parentId
              AND c.status <> 'DELETED'
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findVisibleReplies(@Param("parentId") Integer parentId);

    List<Comment> findByAuthorId(Integer authorId);
    List<Comment> findByStatus(CommentStatus status);
    List<Comment> findByPostIdAndStatus(Integer postId, CommentStatus status);
    long countByPostId(Integer postId);

    @Query("""
            SELECT COUNT(c) FROM Comment c
            WHERE c.postId = :postId
              AND c.status <> 'DELETED'
            """)
    long countVisibleByPostId(@Param("postId") Integer postId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = c.likesCount + 1 WHERE c.commentId = :id")
    void incrementLikes(@Param("id") Integer commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likesCount = GREATEST(c.likesCount - 1, 0) WHERE c.commentId = :id")
    void decrementLikes(@Param("id") Integer commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.status = 'DELETED' WHERE c.parentCommentId = :parentId")
    void softDeleteRepliesByParentId(@Param("parentId") Integer parentId);

    @Transactional
    void deleteByCommentId(Integer commentId);
}
