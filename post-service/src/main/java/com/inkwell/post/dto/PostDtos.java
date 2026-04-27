package com.inkwell.post.dto;

import com.inkwell.post.entity.Post.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * All Post-Service request/response DTOs in one file.
 */
public class PostDtos {

    // ── Create ───────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreatePostRequest {

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title max 255 characters")
        private String title;

        @NotBlank(message = "Content is required")
        private String content;         // rich HTML — sanitised server-side
        private String excerpt;
        private String featuredImageUrl;
    }

    // ── Update ───────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdatePostRequest {

        @NotBlank(message = "Title is required")
        @Size(max = 255)
        private String title;

        @NotBlank(message = "Content is required")
        private String content;
        private String excerpt;
        private String featuredImageUrl;
    }

    // ── Response ─────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PostResponse {
        private Integer       postId;
        private Integer       authorId;
        private String        title;
        private String        slug;
        private String        content;
        private String        excerpt;
        private String        featuredImageUrl;
        private PostStatus    status;
        private Integer       readTimeMin;
        private Integer       viewCount;
        private Integer       likesCount;
        private Boolean       likedByCurrentUser;
        private Boolean       isFeatured;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime publishedAt;
    }

    // ── Summary (feed card — no full content) ─────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PostSummary {
        private Integer       postId;
        private Integer       authorId;
        private String        title;
        private String        slug;
        private String        excerpt;
        private String        featuredImageUrl;
        private Integer       readTimeMin;
        private Integer       viewCount;
        private Integer       likesCount;
        private Boolean       isFeatured;
        private LocalDateTime publishedAt;
    }

    // ── Stats (author analytics) ─────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PostStats {
        private Integer postId;
        private String  title;
        private String  slug;
        private Integer viewCount;
        private Integer likesCount;
        private Long    commentCount;   // supplied by comment-service
        private LocalDateTime publishedAt;
    }
}
