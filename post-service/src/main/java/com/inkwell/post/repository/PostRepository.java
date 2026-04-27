package com.inkwell.post.repository;

import com.inkwell.post.entity.Post;
import com.inkwell.post.entity.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    Optional<Post> findBySlug(String slug);
    Optional<Post> findByPostId(Integer postId);
    List<Post> findByAuthorId(Integer authorId);
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Integer authorId);
    List<Post> findByStatus(PostStatus status);
    Page<Post>  findByStatus(PostStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'PUBLISHED'
            ORDER BY p.isFeatured DESC, p.publishedAt DESC
            """)
    Page<Post> findPublishedOrderByFeaturedAndPublishedAt(Pageable pageable);

    @Query(value = """
            SELECT *
            FROM posts
            WHERE status = 'PUBLISHED'
              AND (LOWER(title) LIKE CONCAT('%', :query, '%')
                OR LOWER(content) LIKE CONCAT('%', :query, '%'))
            ORDER BY published_at DESC
            """, nativeQuery = true)
    List<Post> searchByTitleOrContent(@Param("query") String query);

    @Query("SELECT p FROM Post p WHERE p.postId IN :ids AND p.status = 'PUBLISHED' ORDER BY p.publishedAt DESC")
    List<Post> findPublishedByIds(@Param("ids") List<Integer> ids);

    long countByAuthorId(Integer authorId);
    long countByStatus(PostStatus status);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.postId = :postId")
    void incrementLikes(@Param("postId") Integer postId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likesCount = GREATEST(p.likesCount - 1, 0) WHERE p.postId = :postId")
    void decrementLikes(@Param("postId") Integer postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.likesCount = GREATEST(p.likesCount - :amount, 0) WHERE p.postId = :postId")
    void decrementLikesBy(@Param("postId") Integer postId, @Param("amount") long amount);

    @Transactional
    void deleteByPostId(Integer postId);
}
