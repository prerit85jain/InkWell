package com.inkwell.post.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer postId;
	@Column(nullable = false)
	private Integer authorId;
	@Column(nullable = false)
	private String title;
	@Column(unique = true, nullable = false)
	private String slug;
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String content;
	@Column(length = 500)
	private String excerpt;
	private String featuredImageUrl;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostStatus status;
	private Integer readTimeMin;
	private Integer viewCount;
	private Integer likesCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime publishedAt;
	
	public enum PostStatus{DRAFT, PUBLISHED, UNPUBLISHED, ARCHIVED}
	
}
