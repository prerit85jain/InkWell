package com.inkwell.media.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores metadata for every file uploaded to AWS S3.
 *
 * isDeleted = true is a soft-delete flag that preserves the row
 * after deletion so that any post content still referencing the
 * S3 URL does not produce broken image references immediately.
 * A scheduled cleanup job can hard-delete old soft-deleted rows.
 *
 * linkedPostId links the media file to a specific post (optional).
 * A null linkedPostId means the file is in the uploader's personal
 * media library but not yet embedded in any post.
 */
@Entity
@Table(name = "media",
        indexes = {
                @Index(name = "idx_media_uploader",    columnList = "uploader_id"),
                @Index(name = "idx_media_linked_post", columnList = "linked_post_id"),
                @Index(name = "idx_media_deleted",     columnList = "is_deleted")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Integer mediaId;

    // ── Uploader (FK to auth-service users, stored as plain ID) ──
    @Column(name = "uploader_id", nullable = false)
    private Integer uploaderId;

    // ── File metadata ─────────────────────────────────────────────
    @Column(name = "filename", nullable = false)
    private String filename;          // stored name on S3 (UUID-prefixed)

    @Column(name = "original_name", nullable = false)
    private String originalName;      // original filename from the client

    @Column(name = "url", nullable = false, length = 1000)
    private String url;               // full S3 / CloudFront URL

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;          // e.g. image/jpeg, application/pdf

    @Column(name = "size_kb", nullable = false)
    private Long sizeKb;

    @Column(name = "alt_text", length = 500)
    private String altText;           // accessibility alt text for images

    // ── Post link ─────────────────────────────────────────────────
    @Column(name = "linked_post_id")
    private Integer linkedPostId;     // null until embedded in a post

    // ── Soft delete ───────────────────────────────────────────────
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    // ── Audit ─────────────────────────────────────────────────────
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
