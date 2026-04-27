package com.inkwell.category.repository;

import com.inkwell.category.entity.PostTag;
import com.inkwell.category.entity.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

    List<PostTag> findByPostId(Integer postId);
    List<PostTag> findByTagId(Integer tagId);
    boolean existsByPostIdAndTagId(Integer postId, Integer tagId);

    @Transactional
    void deleteByPostIdAndTagId(Integer postId, Integer tagId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.postId = :postId")
    void deleteAllByPostId(@Param("postId") Integer postId);
}
