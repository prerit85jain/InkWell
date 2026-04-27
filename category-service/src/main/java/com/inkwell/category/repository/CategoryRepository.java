package com.inkwell.category.repository;

import com.inkwell.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findBySlug(String slug);
    Optional<Category> findByCategoryId(Integer categoryId);
    List<Category> findByParentCategoryIdIsNull();
    List<Category> findByParentCategoryId(Integer parentCategoryId);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);

    @Transactional
    void deleteByCategoryId(Integer categoryId);

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.postCount = c.postCount + 1 WHERE c.categoryId = :id")
    void incrementPostCount(@Param("id") Integer categoryId);

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.postCount = GREATEST(c.postCount - 1, 0) WHERE c.categoryId = :id")
    void decrementPostCount(@Param("id") Integer categoryId);

    @Query("""
            SELECT c FROM Category c
            WHERE c.categoryId IN (
                SELECT pc.categoryId FROM PostCategory pc WHERE pc.postId = :postId
            )
            """)
    List<Category> findCategoriesByPostId(@Param("postId") Integer postId);
}
