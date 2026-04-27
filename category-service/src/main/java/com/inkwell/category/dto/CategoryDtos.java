package com.inkwell.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * All Category & Tag Service DTOs.
 */
public class CategoryDtos {

    // ══════════════════════════════════════════
    //  CATEGORY
    // ══════════════════════════════════════════

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateCategoryRequest {

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        /** Null → root category; non-null → child of given parent. */
        private Integer parentCategoryId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateCategoryRequest {

        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private Integer parentCategoryId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CategoryResponse {
        private Integer           categoryId;
        private String            name;
        private String            slug;
        private String            description;
        private Integer           parentCategoryId;
        private Integer           postCount;
        private LocalDateTime     createdAt;
        private List<CategoryResponse> children;   // populated for hierarchical view
    }

    // ══════════════════════════════════════════
    //  TAG
    // ══════════════════════════════════════════

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateTagRequest {

        @NotBlank(message = "Tag name is required")
        @Size(max = 60, message = "Tag name max 60 characters")
        private String name;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TagResponse {
        private Integer       tagId;
        private String        name;
        private String        slug;
        private Integer       postCount;
        private LocalDateTime createdAt;
    }

    // ══════════════════════════════════════════
    //  POST ASSIGNMENT
    // ══════════════════════════════════════════

    /** Request body to assign a list of category IDs to a post. */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignCategoriesRequest {
        private List<Integer> categoryIds;
    }

    /** Request body to assign a list of tag IDs to a post. */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AssignTagsRequest {
        private List<Integer> tagIds;
    }
}
