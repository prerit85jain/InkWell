package com.inkwell.category.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Join table linking a post (from post-service) to a tag.
 */
@Entity
@Table(name = "post_tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_pt_post", columnList = "post_id"),
                @Index(name = "idx_pt_tag",  columnList = "tag_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PostTagId.class)
public class PostTag {

    @Id
    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private Integer tagId;
}
