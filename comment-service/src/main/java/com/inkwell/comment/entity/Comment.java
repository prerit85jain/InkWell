package com.inkwell.comment.entity;

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
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer commentId;
	@Column(nullable = false)
	private Integer postId;
	@Column(nullable = false)
	private Integer authorId;
	private Integer parentCommentId;
	@Lob
	@Column(columnDefinition ="TEXT")
	private String content;
	private Integer likeCount=0;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CommentStatus status;
	private LocalDateTime  createdAt;
	private LocalDateTime updatedAt;
	
	public enum CommentStatus{APPROVED, PENDING, REJECTED, DELETED}
	
	
}
