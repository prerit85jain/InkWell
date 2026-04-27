package com.inkwell.comment.service;

import com.inkwell.comment.dto.CommentDtos.*;
import com.inkwell.comment.entity.Comment;

import java.util.List;
import java.util.Optional;

/**
 * Business contract for comment management.
 */
public interface CommentService {

    // ── CRUD ─────────────────────────────────────────────────────
    CommentResponse addComment(Integer postId, Integer authorId, AddCommentRequest request);

    Optional<CommentResponse> getCommentById(Integer commentId);

    /** Top-level comments + their replies as threaded view for a post. */
    List<CommentThread> getThreadedCommentsByPost(Integer postId);

    /** Flat list of all visible comments for a post. */
    List<CommentResponse> getCommentsByPost(Integer postId);

    /** All replies to a specific parent comment. */
    List<CommentResponse> getReplies(Integer parentCommentId);

    CommentResponse updateComment(Integer commentId, Integer requestingUserId,
                                  UpdateCommentRequest request);

    /** Soft-deletes the comment and all its child replies. */
    void deleteComment(Integer commentId, Integer requestingUserId);

    // ── Moderation ───────────────────────────────────────────────
    CommentResponse approveComment(Integer commentId);

    CommentResponse rejectComment(Integer commentId);

    /** All comments for a post including pending (author / admin view). */
    List<CommentResponse> getCommentsByPostForModeration(Integer postId);

    /** All PENDING comments across the platform (Admin only). */
    List<CommentResponse> getPendingComments();

    // ── Engagement ───────────────────────────────────────────────
    void likeComment(Integer commentId, Integer userId);

    void unlikeComment(Integer commentId, Integer userId);

    // ── Counts ───────────────────────────────────────────────────
    long getCommentCount(Integer postId);
}
