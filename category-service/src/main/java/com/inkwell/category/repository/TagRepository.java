package com.inkwell.category.repository;

import com.inkwell.category.entity.Tag;
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
public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findBySlug(String slug);
    Optional<Tag> findByTagId(Integer tagId);
    Optional<Tag> findByName(String name);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);

    @Transactional
    void deleteByTagId(Integer tagId);

    @Query("SELECT t FROM Tag t ORDER BY t.postCount DESC")
    List<Tag> findTopTags(Pageable pageable);

    @Query("""
            SELECT t FROM Tag t
            WHERE t.tagId IN (
                SELECT pt.tagId FROM PostTag pt WHERE pt.postId = :postId
            )
            """)
    List<Tag> findTagsByPostId(@Param("postId") Integer postId);

    @Transactional
    @Modifying
    @Query("UPDATE Tag t SET t.postCount = t.postCount + 1 WHERE t.tagId = :id")
    void incrementPostCount(@Param("id") Integer tagId);

    @Transactional
    @Modifying
    @Query("UPDATE Tag t SET t.postCount = GREATEST(t.postCount - 1, 0) WHERE t.tagId = :id")
    void decrementPostCount(@Param("id") Integer tagId);
}
