package com.inkwell.post.repository;

import com.inkwell.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {

    boolean existsByPostIdAndUserId(Integer postId, Integer userId);

    long countByPostIdAndUserId(Integer postId, Integer userId);

    Optional<PostLike> findByPostIdAndUserId(Integer postId, Integer userId);

    @Transactional
    @Modifying
    @Query(value = """
        DELETE FROM post_likes 
        WHERE post_id = :postId AND user_id = :userId
        """, nativeQuery = true)
    int deleteAndReturnCount(@Param("postId") Integer postId, @Param("userId") Integer userId);

    long countByPostId(Integer postId);
}
