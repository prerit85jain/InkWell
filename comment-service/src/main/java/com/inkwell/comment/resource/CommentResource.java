package com.inkwell.comment.resource;

import com.inkwell.comment.dto.CommentDtos.*;
import com.inkwell.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for comment management.
 * Base path: /comments
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Threaded comments, moderation, like/unlike APIs")
public class CommentResource {

    private final CommentService commentService;

    // ── Public read ───────────────────────────────────────────────

    @GetMapping("/post/{postId}/thread")
    @Operation(summary = "Get threaded comments for a post (top-level + replies)")
    public ResponseEntity<List<CommentThread>> getThreaded(@PathVariable Integer postId) {
        return ResponseEntity.ok(commentService.getThreadedCommentsByPost(postId));
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get flat list of visible comments for a post")
    public ResponseEntity<List<CommentResponse>> getByPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @GetMapping("/count/{postId}")
    @Operation(summary = "Get visible comment count for a post")
    public ResponseEntity<Map<String, Long>> getCount(@PathVariable Integer postId) {
        return ResponseEntity.ok(Map.of("count", commentService.getCommentCount(postId)));
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Get a single comment by ID")
    public ResponseEntity<CommentResponse> getById(@PathVariable Integer commentId) {
        return commentService.getCommentById(commentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Get all replies to a comment")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Integer commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    // ── Authenticated write ───────────────────────────────────────

    @PostMapping("/post/{postId}")
    @Operation(summary = "Add a top-level comment or reply to a post")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Integer postId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication auth) {
        Integer authorId = extractUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(postId, authorId, request));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Edit own comment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Integer commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                commentService.updateComment(commentId, extractUserId(auth), request));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete own comment (soft-delete + child replies)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Integer commentId,
            Authentication auth) {
        commentService.deleteComment(commentId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Comment deleted"));
    }

    // ── Engagement ────────────────────────────────────────────────

    @PostMapping("/{commentId}/like")
    @Operation(summary = "Like a comment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> like(
            @PathVariable Integer commentId,
            Authentication auth) {
        commentService.likeComment(commentId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Comment liked"));
    }

    @DeleteMapping("/{commentId}/like")
    @Operation(summary = "Unlike a comment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> unlike(
            @PathVariable Integer commentId,
            Authentication auth) {
        commentService.unlikeComment(commentId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Comment unliked"));
    }

    // ── Author moderation ─────────────────────────────────────────

    @GetMapping("/post/{postId}/moderate")
    @Operation(summary = "Get all comments for a post including pending (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<List<CommentResponse>> getForModeration(
            @PathVariable Integer postId) {
        return ResponseEntity.ok(
                commentService.getCommentsByPostForModeration(postId));
    }

    @PutMapping("/{commentId}/approve")
    @Operation(summary = "Approve a pending comment (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<CommentResponse> approve(@PathVariable Integer commentId) {
        return ResponseEntity.ok(commentService.approveComment(commentId));
    }

    @PutMapping("/{commentId}/reject")
    @Operation(summary = "Reject a comment (Author / Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<CommentResponse> reject(@PathVariable Integer commentId) {
        return ResponseEntity.ok(commentService.rejectComment(commentId));
    }

    // ── Admin ─────────────────────────────────────────────────────

    @GetMapping("/admin/pending")
    @Operation(summary = "Get all PENDING comments platform-wide (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentResponse>> getAllPending() {
        return ResponseEntity.ok(commentService.getPendingComments());
    }

    // ── Helper ───────────────────────────────────────────────────

    private Integer extractUserId(Authentication auth) {
        Object creds = auth.getCredentials();
        if (creds instanceof Integer) return (Integer) creds;
        throw new IllegalStateException("Could not extract userId from JWT");
    }
}
