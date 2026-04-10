package com.inkwell.comment.service;

import java.util.List;
import java.util.Optional;

import com.inkwell.comment.entity.Comment;

public interface CommentService {
	Comment add(Comment comment);
	List<Comment> getByPost(Integer postId);
	Optional<Comment> getByCommentId(Integer commentId);
	List<Comment> getReplies(Integer parentId);
	Comment update(Integer commentId, String content);
	void delete(Integer commentId);
	void approve(Integer commentId);
	void reject(Integer commentId);
	void like(Integer commentId);
	void unlike(Integer commentId);
	long count(Integer postId);
	List<Comment> getPending();
}
