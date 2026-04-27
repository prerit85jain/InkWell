package com.inkwell.category.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Join table linking a post (from post-service) to a category.
 * Cross-service relationship managed by IDs only — no JPA FK to post-service.
 */
@Entity
@Table(name = "post_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "category_id"}),
        indexes = {
                @Index(name = "idx_pc_post",     columnList = "post_id"),
                @Index(name = "idx_pc_category", columnList = "category_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PostCategoryId.class)
public class PostCategory {

    @Id
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Id
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
}
