package com.inkwell.comment.dto;

import com.inkwell.comment.entity.Comment.CommentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * All Comment-Service request / response DTOs.
 */
public class CommentDtos {

    // ── Add Comment / Reply ───────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddCommentRequest {

        @NotBlank(message = "Comment content is required")
        @Size(max = 2000, message = "Comment must be 2000 characters or fewer")
        private String content;

        /** null for top-level comment; parent commentId for replies */
        private Integer parentCommentId;
    }

    // ── Update ───────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateCommentRequest {

        @NotBlank(message = "Content is required")
        @Size(max = 2000)
        private String content;
    }

    // ── Response (single comment, no replies nested) ──────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CommentResponse {
        private Integer       commentId;
        private Integer       postId;
        private Integer       authorId;
        private String        authorName;   // fetched from auth-service
        private Integer       parentCommentId;
        private String        content;
        private Integer       likesCount;
        private CommentStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ── Thread (top-level comment + its replies) ──────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CommentThread {
        private CommentResponse       comment;
        private List<CommentResponse> replies;
    }
}
