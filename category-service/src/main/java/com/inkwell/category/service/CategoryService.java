package com.inkwell.category.service;

import com.inkwell.category.dto.CategoryDtos.*;

import java.util.List;
import java.util.Optional;

/**
 * Business contract for the Category & Tag taxonomy service.
 */
public interface CategoryService {

    // ── Categories ───────────────────────────────────────────────
    CategoryResponse createCategory(CreateCategoryRequest request);

    Optional<CategoryResponse> getCategoryBySlug(String slug);

    Optional<CategoryResponse> getCategoryById(Integer categoryId);

    /** All root categories with their children populated. */
    List<CategoryResponse> getAllCategoriesHierarchical();

    /** Flat list of all categories. */
    List<CategoryResponse> getAllCategories();

    /** Children of a given parent category. */
    List<CategoryResponse> getChildCategories(Integer parentCategoryId);

    CategoryResponse updateCategory(Integer categoryId, UpdateCategoryRequest request);

    void deleteCategory(Integer categoryId);

    // ── Tags ─────────────────────────────────────────────────────
    TagResponse createTag(CreateTagRequest request);

    Optional<TagResponse> getTagBySlug(String slug);

    Optional<TagResponse> getTagById(Integer tagId);

    List<TagResponse> getAllTags();

    /** Top N trending tags ordered by postCount DESC. */
    List<TagResponse> getTrendingTags(int limit);

    void deleteTag(Integer tagId);

    // ── Post ↔ Category assignment ───────────────────────────────
    void addCategoryToPost(Integer postId, Integer categoryId);

    void removeCategoryFromPost(Integer postId, Integer categoryId);

    List<CategoryResponse> getCategoriesByPost(Integer postId);

    List<Integer> getPostIdsByCategory(Integer categoryId);

    void assignCategoriesToPost(Integer postId, List<Integer> categoryIds);

    // ── Post ↔ Tag assignment ────────────────────────────────────
    void addTagToPost(Integer postId, Integer tagId);

    void removeTagFromPost(Integer postId, Integer tagId);

    List<TagResponse> getTagsByPost(Integer postId);

    List<Integer> getPostIdsByTag(Integer tagId);

    void assignTagsToPost(Integer postId, List<Integer> tagIds);

    /** Remove all category and tag associations for a deleted post. */
    void removeAllAssociationsForPost(Integer postId);
}
