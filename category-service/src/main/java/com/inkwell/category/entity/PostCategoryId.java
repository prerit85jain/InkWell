package com.inkwell.category.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostCategoryId implements Serializable {
    private Integer postId;
    private Integer categoryId;
}
