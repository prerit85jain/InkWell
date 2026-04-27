package com.inkwell.category.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Flat keyword tag attached to posts for discovery.
 *
 * postCount accumulates each time a tag is linked to a post and
 * decrements on removal — used to surface trending tags.
 *
 * slug is unique platform-wide and auto-generated from the tag name.
 */
@Entity
@Table(name = "tags",
        indexes = {
                @Index(name = "idx_tag_slug",       columnList = "slug",       unique = true),
                @Index(name = "idx_tag_post_count", columnList = "post_count")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer tagId;

    @NotBlank
    @Size(max = 60)
    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 80)
    private String slug;

    /** Number of published posts currently tagged with this tag. */
    @Column(name = "post_count", nullable = false)
    @Builder.Default
    private Integer postCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
