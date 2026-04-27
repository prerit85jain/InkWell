package com.inkwell.category.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Hierarchical content category.
 *
 * parentCategoryId == null  →  root-level category
 * parentCategoryId != null  →  child category (one level of nesting supported)
 *
 * slug must be unique across the platform and is auto-generated from name.
 * postCount is a denormalised counter updated when posts are assigned / removed.
 */
@Entity
@Table(name = "categories",
        indexes = {
                @Index(name = "idx_category_slug",   columnList = "slug",   unique = true),
                @Index(name = "idx_category_parent", columnList = "parent_category_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    /** Null for root categories; parent's ID for child categories. */
    @Column(name = "parent_category_id")
    private Integer parentCategoryId;

    /** Denormalised count of published posts in this category. */
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
