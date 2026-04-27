package com.inkwell.category.repository;

import com.inkwell.category.entity.PostCategory;
import com.inkwell.category.entity.PostCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, PostCategoryId> {

    List<PostCategory> findByPostId(Integer postId);
    List<PostCategory> findByCategoryId(Integer categoryId);
    boolean existsByPostIdAndCategoryId(Integer postId, Integer categoryId);

    @Transactional
    void deleteByPostIdAndCategoryId(Integer postId, Integer categoryId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PostCategory pc WHERE pc.postId = :postId")
    void deleteAllByPostId(@Param("postId") Integer postId);
}
