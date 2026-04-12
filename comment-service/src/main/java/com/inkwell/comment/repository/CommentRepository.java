package com.inkwell.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.Comment.CommentStatus;

import jakarta.transaction.Transactional;

public interface CommentRepository extends JpaRepository<Comment, Integer>{

	List<Comment> findByPostIdAndStatusNot(Integer postId, CommentStatus status);
	List<Comment> findByAuthorId(Integer authorId);
	Comment findByCommentId(Integer commentId);
	List<Comment> findByParentCommentId(Integer parentCommentId);
	long countByPostId(Integer postId);
	List<Comment> findByStatus(CommentStatus status);
	void deleteByCommentId(Integer commentId);
	@Modifying
	@Transactional
	@Query("UPDATE Comment c SET c.likeCount=c.likeCount+1 WHERE c.commentId=:id")
	void incementLikes(Integer commentId);
	@Modifying
	@Transactional
	@Query("UPDATE Comment c SET c.likeCount=c.likeCount-1 WHERE c.commentId=:id")
	void decrementLikes(Integer commentId);
}
