package com.inkwell.comment.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.Comment.CommentStatus;
import com.inkwell.comment.repository.CommentRepository;
import com.inkwell.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService  {
	@Autowired
	CommentRepository commentRepo;
	
	@Override
	public Comment add(Comment comment) {
		comment.setStatus(CommentStatus.APPROVED);
		comment.setCreatedAt(LocalDateTime.now());
		comment.setLikeCount(0);
		return commentRepo.save(comment);
	}

	@Override
	public List<Comment> getByPost(Integer postId) {
		return commentRepo.findByPostIdAndStatusNot(postId, CommentStatus.DELETED);
	}

	@Override
	public Optional<Comment> getByCommentId(Integer commentId) {
		return commentRepo.findById(commentId);
	}

	@Override
	public List<Comment> getReplies(Integer parentId) {
		return commentRepo.findByParentCommentId(parentId);
	}

	@Override
	public Comment update(Integer commentId, String content) {
		Comment comment = getByCommentId(commentId).orElseThrow(()-> new RuntimeException("Comment not found"));
		comment.setContent(content);
		comment.setUpdatedAt(LocalDateTime.now());
		return commentRepo.save(comment);
	}

	@Override
	public void delete(Integer commentId) {
		commentRepo.findByParentCommentId(commentId).forEach(r->{
			r.setStatus(CommentStatus.DELETED);
			commentRepo.save(r);
		});
		Comment comment = getByCommentId(commentId).orElseThrow(()->new RuntimeException("Comment not found"));
		comment.setStatus(CommentStatus.DELETED);
		commentRepo.save(comment);
	}

	@Override
	public void approve(Integer commentId) {
		Comment comment = getByCommentId(commentId).orElseThrow(()->new RuntimeException("Comment not found"));
		comment.setStatus(CommentStatus.APPROVED);
		commentRepo.save(comment);
	}

	@Override
	public void reject(Integer commentId) {
		Comment comment = getByCommentId(commentId).orElseThrow(()->new RuntimeException("Comment not found"));
		comment.setStatus(CommentStatus.REJECTED);
		commentRepo.save(comment);
	}

	@Override
	public void like(Integer commentId) {
		commentRepo.incementLikes(commentId);
	}

	@Override
	public void unlike(Integer commentId) {
		commentRepo.decrementLikes(commentId);
	}

	@Override
	public long count(Integer postId) {
		return commentRepo.countByPostId(postId);
	}

	@Override
	public List<Comment> getPending() {
		return commentRepo.findByStatus(CommentStatus.PENDING);
	}

}
