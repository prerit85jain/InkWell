package com.inkwell.comment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a threaded comment on a blog post.
 *
 * Threading:
 *   parentCommentId == null  →  top-level comment
 *   parentCommentId != null  →  reply (max one level deep)
 *
 * Soft-delete:
 *   status = DELETED preserves the row so child replies retain their
 *   parentCommentId reference and thread context remains visible in the UI.
 *
 * Moderation:
 *   Default status = APPROVED (platform flag can switch to PENDING).
 *   Authors moderate their own posts; Admins moderate everything.
 */
@Entity
@Table(name = "comments",
        indexes = {
                @Index(name = "idx_comment_post",    columnList = "post_id"),
                @Index(name = "idx_comment_author",  columnList = "author_id"),
                @Index(name = "idx_comment_parent",  columnList = "parent_comment_id"),
                @Index(name = "idx_comment_status",  columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    // ── Relationships (IDs — cross-service, no JPA FK) ────────────
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Column(name = "author_id", nullable = false)
    private Integer authorId;

    /** Null for top-level comments; parent's ID for replies. */
    @Column(name = "parent_comment_id")
    private Integer parentCommentId;

    // ── Content ───────────────────────────────────────────────────
    @NotBlank
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // ── Engagement ────────────────────────────────────────────────
    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Integer likesCount = 0;

    // ── Moderation ────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private CommentStatus status = CommentStatus.APPROVED;

    // ── Audit ─────────────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Status enum ───────────────────────────────────────────────
    public enum CommentStatus {
        APPROVED,
        PENDING,
        REJECTED,
        DELETED
    }
}
