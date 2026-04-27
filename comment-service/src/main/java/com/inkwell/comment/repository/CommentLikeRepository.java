package com.inkwell.comment.repository;

import com.inkwell.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Integer> {

    boolean existsByCommentIdAndUserId(Integer commentId, Integer userId);

    Optional<CommentLike> findByCommentIdAndUserId(Integer commentId, Integer userId);

    @Transactional
    @Modifying
    @Query(value = """
        DELETE FROM comment_likes 
        WHERE comment_id = :commentId AND user_id = :userId
        """, nativeQuery = true)
    int deleteAndReturnCount(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
}