package com.inkwell.category.service.impl;

import com.github.slugify.Slugify;
import com.inkwell.category.dto.CategoryDtos.*;
import com.inkwell.category.entity.*;
import com.inkwell.category.exception.CategoryException;
import com.inkwell.category.repository.*;
import com.inkwell.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository    categoryRepository;
    private final TagRepository         tagRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostTagRepository      postTagRepository;

    private static final Slugify SLUGIFY = Slugify.builder().build();

    // ══════════════════════════════════════════
    //  CATEGORIES
    // ══════════════════════════════════════════

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryException("Category already exists: " + request.getName());
        }
        String slug = resolveUniqueCategorySlug(request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .parentCategoryId(request.getParentCategoryId())
                .build();

        return toCategoryResponse(categoryRepository.save(category), false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(c -> toCategoryResponse(c, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryById(Integer categoryId) {
        return categoryRepository.findByCategoryId(categoryId)
                .map(c -> toCategoryResponse(c, false));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesHierarchical() {
        // Load root categories then attach children
        return categoryRepository.findByParentCategoryIdIsNull().stream()
                .map(root -> {
                    List<CategoryResponse> children =
                            categoryRepository.findByParentCategoryId(root.getCategoryId())
                                    .stream()
                                    .map(c -> toCategoryResponse(c, false))
                                    .toList();
                    CategoryResponse resp = toCategoryResponse(root, false);
                    resp.setChildren(children);
                    return resp;
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> toCategoryResponse(c, false)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(Integer parentCategoryId) {
        return categoryRepository.findByParentCategoryId(parentCategoryId)
                .stream().map(c -> toCategoryResponse(c, false)).toList();
    }

    @Override
    public CategoryResponse updateCategory(Integer categoryId,
                                           UpdateCategoryRequest request) {
        Category category = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new CategoryException("Category not found: " + categoryId));

        if (StringUtils.hasText(request.getName())
                && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new CategoryException("Category name already in use: " + request.getName());
            }
            category.setName(request.getName());
            category.setSlug(resolveUniqueCategorySlug(request.getName()));
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getParentCategoryId() != null) {
            category.setParentCategoryId(request.getParentCategoryId());
        }
        return toCategoryResponse(categoryRepository.save(category), false);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        categoryRepository.deleteByCategoryId(categoryId);
    }

    // ══════════════════════════════════════════
    //  TAGS
    // ══════════════════════════════════════════

    @Override
    public TagResponse createTag(CreateTagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new CategoryException("Tag already exists: " + request.getName());
        }
        String slug = resolveUniqueTagSlug(request.getName());

        Tag tag = Tag.builder()
                .name(request.getName())
                .slug(slug)
                .build();

        return toTagResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TagResponse> getTagBySlug(String slug) {
        return tagRepository.findBySlug(slug).map(this::toTagResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TagResponse> getTagById(Integer tagId) {
        return tagRepository.findByTagId(tagId).map(this::toTagResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream().map(this::toTagResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTrendingTags(int limit) {
        return tagRepository.findTopTags(PageRequest.of(0, limit))
                .stream().map(this::toTagResponse).toList();
    }

    @Override
    public void deleteTag(Integer tagId) {
        tagRepository.deleteByTagId(tagId);
    }

    // ══════════════════════════════════════════
    //  POST ↔ CATEGORY ASSIGNMENT
    // ══════════════════════════════════════════

    @Override
    public void addCategoryToPost(Integer postId, Integer categoryId) {
        if (postCategoryRepository.existsByPostIdAndCategoryId(postId, categoryId)) return;

        postCategoryRepository.save(
                PostCategory.builder().postId(postId).categoryId(categoryId).build());
        categoryRepository.incrementPostCount(categoryId);
    }

    @Override
    public void removeCategoryFromPost(Integer postId, Integer categoryId) {
        if (!postCategoryRepository.existsByPostIdAndCategoryId(postId, categoryId)) return;

        postCategoryRepository.deleteByPostIdAndCategoryId(postId, categoryId);
        categoryRepository.decrementPostCount(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByPost(Integer postId) {
        return categoryRepository.findCategoriesByPostId(postId)
                .stream().map(c -> toCategoryResponse(c, false)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getPostIdsByCategory(Integer categoryId) {
        return postCategoryRepository.findByCategoryId(categoryId).stream()
                .map(PostCategory::getPostId)
                .distinct()
                .toList();
    }

    @Override
    public void assignCategoriesToPost(Integer postId, List<Integer> categoryIds) {
        // Remove existing associations first
        List<PostCategory> existing = postCategoryRepository.findByPostId(postId);
        existing.forEach(pc -> categoryRepository.decrementPostCount(pc.getCategoryId()));
        postCategoryRepository.deleteAllByPostId(postId);

        // Assign new set
        categoryIds.forEach(catId -> addCategoryToPost(postId, catId));
    }

    // ══════════════════════════════════════════
    //  POST ↔ TAG ASSIGNMENT
    // ══════════════════════════════════════════

    @Override
    public void addTagToPost(Integer postId, Integer tagId) {
        if (postTagRepository.existsByPostIdAndTagId(postId, tagId)) return;

        postTagRepository.save(PostTag.builder().postId(postId).tagId(tagId).build());
        tagRepository.incrementPostCount(tagId);
    }

    @Override
    public void removeTagFromPost(Integer postId, Integer tagId) {
        if (!postTagRepository.existsByPostIdAndTagId(postId, tagId)) return;

        postTagRepository.deleteByPostIdAndTagId(postId, tagId);
        tagRepository.decrementPostCount(tagId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByPost(Integer postId) {
        return tagRepository.findTagsByPostId(postId)
                .stream().map(this::toTagResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getPostIdsByTag(Integer tagId) {
        return postTagRepository.findByTagId(tagId).stream()
                .map(PostTag::getPostId)
                .distinct()
                .toList();
    }

    @Override
    public void assignTagsToPost(Integer postId, List<Integer> tagIds) {
        List<PostTag> existing = postTagRepository.findByPostId(postId);
        existing.forEach(pt -> tagRepository.decrementPostCount(pt.getTagId()));
        postTagRepository.deleteAllByPostId(postId);

        tagIds.forEach(tagId -> addTagToPost(postId, tagId));
    }

    @Override
    public void removeAllAssociationsForPost(Integer postId) {
        // Decrement counts for all categories
        postCategoryRepository.findByPostId(postId)
                .forEach(pc -> categoryRepository.decrementPostCount(pc.getCategoryId()));
        postCategoryRepository.deleteAllByPostId(postId);

        // Decrement counts for all tags
        postTagRepository.findByPostId(postId)
                .forEach(pt -> tagRepository.decrementPostCount(pt.getTagId()));
        postTagRepository.deleteAllByPostId(postId);
    }

    // ══════════════════════════════════════════
    //  SLUG HELPERS
    // ══════════════════════════════════════════

    private String resolveUniqueCategorySlug(String name) {
        String base = SLUGIFY.slugify(name);
        String candidate = base;
        int attempt = 0;
        while (categoryRepository.existsBySlug(candidate)) {
            candidate = base + "-" + (++attempt);
        }
        return candidate;
    }

    private String resolveUniqueTagSlug(String name) {
        String base = SLUGIFY.slugify(name);
        String candidate = base;
        int attempt = 0;
        while (tagRepository.existsBySlug(candidate)) {
            candidate = base + "-" + (++attempt);
        }
        return candidate;
    }

    // ══════════════════════════════════════════
    //  MAPPING
    // ══════════════════════════════════════════

    private CategoryResponse toCategoryResponse(Category c, boolean withChildren) {
        return CategoryResponse.builder()
                .categoryId(c.getCategoryId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .parentCategoryId(c.getParentCategoryId())
                .postCount(c.getPostCount())
                .createdAt(c.getCreatedAt())
                .children(List.of())
                .build();
    }

    private TagResponse toTagResponse(Tag t) {
        return TagResponse.builder()
                .tagId(t.getTagId())
                .name(t.getName())
                .slug(t.getSlug())
                .postCount(t.getPostCount())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
