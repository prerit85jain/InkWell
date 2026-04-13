package com.inkwell.category.service;

import java.util.List;
import java.util.Optional;

import com.inkwell.category.entity.Category;
import com.inkwell.category.entity.Tag;

public interface CategoryService {
	Category createCategory(Category category);
	List<Category> getAllCategories();
	Optional<Category> getBySlug(String slug);
    Category updateCategory(Integer id, Category c);
    void deleteCategory(Integer id);
    Tag createTag(Tag t);
    List<Tag> getAllTags();
    List<Tag> getTrending(int limit);
    void deleteTag(Integer id);
    void addTagToPost(Integer postId, Integer tagId);
    void removeTagFromPost(Integer postId, Integer tagId);
    List<Tag> getTagsByPost(Integer postId);
}
