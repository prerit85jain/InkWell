package com.inkwell.category.resource;

import com.inkwell.category.dto.CategoryDtos.*;
import com.inkwell.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for category and tag taxonomy management.
 *
 * Category base path : /categories
 * Tag base path      : /tags
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Taxonomy", description = "Category and tag management, post-taxonomy assignments")
public class CategoryResource {

    private final CategoryService categoryService;

    // ══════════════════════════════════════════
    //  CATEGORIES  —  /categories
    // ══════════════════════════════════════════

    @GetMapping("/categories")
    @Operation(summary = "Get all categories (hierarchical — root with children)")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategoriesHierarchical());
    }

    @GetMapping("/categories/flat")
    @Operation(summary = "Get all categories as a flat list")
    public ResponseEntity<List<CategoryResponse>> getAllFlat() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Integer categoryId) {
        return categoryService.getCategoryById(categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories/slug/{slug}")
    @Operation(summary = "Get category by SEO slug")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories/{parentId}/children")
    @Operation(summary = "Get child categories of a parent")
    public ResponseEntity<List<CategoryResponse>> getChildren(
            @PathVariable Integer parentId) {
        return ResponseEntity.ok(categoryService.getChildCategories(parentId));
    }

    @GetMapping("/categories/post/{postId}")
    @Operation(summary = "Get categories assigned to a post")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByPost(
            @PathVariable Integer postId) {
        return ResponseEntity.ok(categoryService.getCategoriesByPost(postId));
    }

    @GetMapping("/categories/{categoryId}/post-ids")
    @Operation(summary = "Get post IDs assigned to a category")
    public ResponseEntity<List<Integer>> getPostIdsByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(categoryService.getPostIdsByCategory(categoryId));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new category (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    @Operation(summary = "Update a category (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    @Operation(summary = "Delete a category (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }

    // ── Post ↔ Category assignment ────────────────────────────────

    @PostMapping("/categories/{categoryId}/posts/{postId}")
    @Operation(summary = "Assign a category to a post (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> addCategoryToPost(
            @PathVariable Integer categoryId,
            @PathVariable Integer postId) {
        categoryService.addCategoryToPost(postId, categoryId);
        return ResponseEntity.ok(Map.of("message", "Category assigned to post"));
    }

    @DeleteMapping("/categories/{categoryId}/posts/{postId}")
    @Operation(summary = "Remove a category from a post (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> removeCategoryFromPost(
            @PathVariable Integer categoryId,
            @PathVariable Integer postId) {
        categoryService.removeCategoryFromPost(postId, categoryId);
        return ResponseEntity.ok(Map.of("message", "Category removed from post"));
    }

    @PutMapping("/categories/posts/{postId}/assign")
    @Operation(summary = "Replace all categories for a post (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> assignCategories(
            @PathVariable Integer postId,
            @RequestBody AssignCategoriesRequest request) {
        categoryService.assignCategoriesToPost(postId, request.getCategoryIds());
        return ResponseEntity.ok(Map.of("message", "Categories assigned"));
    }

    // ══════════════════════════════════════════
    //  TAGS  —  /tags
    // ══════════════════════════════════════════

    @GetMapping("/tags")
    @Operation(summary = "Get all tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(categoryService.getAllTags());
    }

    @GetMapping("/tags/trending")
    @Operation(summary = "Get top trending tags by usage count")
    public ResponseEntity<List<TagResponse>> getTrending(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(categoryService.getTrendingTags(limit));
    }

    @GetMapping("/tags/{tagId}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Integer tagId) {
        return categoryService.getTagById(tagId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tags/slug/{slug}")
    @Operation(summary = "Get tag by SEO slug")
    public ResponseEntity<TagResponse> getTagBySlug(@PathVariable String slug) {
        return categoryService.getTagBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tags/post/{postId}")
    @Operation(summary = "Get tags assigned to a post")
    public ResponseEntity<List<TagResponse>> getTagsByPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(categoryService.getTagsByPost(postId));
    }

    @GetMapping("/tags/{tagId}/post-ids")
    @Operation(summary = "Get post IDs assigned to a tag")
    public ResponseEntity<List<Integer>> getPostIdsByTag(@PathVariable Integer tagId) {
        return ResponseEntity.ok(categoryService.getPostIdsByTag(tagId));
    }

    @PostMapping("/tags")
    @Operation(summary = "Create a new tag (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createTag(request));
    }

    @DeleteMapping("/tags/{tagId}")
    @Operation(summary = "Delete a tag (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable Integer tagId) {
        categoryService.deleteTag(tagId);
        return ResponseEntity.ok(Map.of("message", "Tag deleted"));
    }

    // ── Post ↔ Tag assignment ──────────────────────────────────────

    @PostMapping("/tags/{tagId}/posts/{postId}")
    @Operation(summary = "Assign a tag to a post (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> addTagToPost(
            @PathVariable Integer tagId,
            @PathVariable Integer postId) {
        categoryService.addTagToPost(postId, tagId);
        return ResponseEntity.ok(Map.of("message", "Tag assigned to post"));
    }

    @DeleteMapping("/tags/{tagId}/posts/{postId}")
    @Operation(summary = "Remove a tag from a post (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> removeTagFromPost(
            @PathVariable Integer tagId,
            @PathVariable Integer postId) {
        categoryService.removeTagFromPost(postId, tagId);
        return ResponseEntity.ok(Map.of("message", "Tag removed from post"));
    }

    @PutMapping("/tags/posts/{postId}/assign")
    @Operation(summary = "Replace all tags for a post (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> assignTags(
            @PathVariable Integer postId,
            @RequestBody AssignTagsRequest request) {
        categoryService.assignTagsToPost(postId, request.getTagIds());
        return ResponseEntity.ok(Map.of("message", "Tags assigned"));
    }

    // ── Post deletion cleanup (called by post-service) ────────────

    @DeleteMapping("/posts/{postId}/associations")
    @Operation(summary = "Remove all category+tag associations for a deleted post (internal)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> removeAllAssociations(
            @PathVariable Integer postId) {
        categoryService.removeAllAssociationsForPost(postId);
        return ResponseEntity.ok(Map.of("message", "All associations removed"));
    }
}
