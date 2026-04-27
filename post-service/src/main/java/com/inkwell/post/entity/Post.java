package com.inkwell.post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Core blog post entity.
 *
 * Lifecycle:  DRAFT → PUBLISHED ↔ UNPUBLISHED → ARCHIVED
 *
 * slug        – URL-safe unique identifier auto-generated from title via Slugify.
 * readTimeMin – computed server-side (wordCount / 200 WPM) on every save.
 * viewCount   – incremented atomically (DB-level) once per unique session.
 * likesCount  – denormalised counter updated by likePost / unlikePost.
 */
@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_post_slug",      columnList = "slug",      unique = true),
                @Index(name = "idx_post_author",    columnList = "author_id"),
                @Index(name = "idx_post_status",    columnList = "status"),
                @Index(name = "idx_post_published", columnList = "published_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer postId;

    // ── Author (FK to auth-service users table) ───────────────────
    @Column(name = "author_id", nullable = false)
    private Integer authorId;

    // ── Content ───────────────────────────────────────────────────
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 300)
    private String slug;

    @NotBlank(message = "Content is required")
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;           // sanitised rich HTML

    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @Column(name = "featured_image_url")
    private String featuredImageUrl;

    // ── Lifecycle ─────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    // ── Computed fields ───────────────────────────────────────────
    @Column(name = "read_time_min")
    @Builder.Default
    private Integer readTimeMin = 1;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Integer likesCount = 0;

    // ── Feature flag (Admin pins post to top of feed) ─────────────
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    // ── Audit ─────────────────────────────────────────────────────
    @Column(name = "created_at",   nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

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
    public enum PostStatus {
        DRAFT,
        PUBLISHED,
        UNPUBLISHED,
        ARCHIVED
    }
}
