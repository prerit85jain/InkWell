package com.inkwell.comment.service.impl;

import com.inkwell.comment.dto.CommentDtos.*;
import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.entity.CommentLike;
import com.inkwell.comment.entity.Comment.CommentStatus;
import com.inkwell.comment.exception.CommentException;
import com.inkwell.comment.repository.CommentRepository;
import com.inkwell.comment.repository.CommentLikeRepository;
import com.inkwell.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RabbitTemplate    rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.comment-added}")
    private String commentAddedKey;

    @Value("${app.services.auth-service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<Integer, String> authorNameCache = new ConcurrentHashMap<>();

    private String getAuthorName(Integer authorId) {
        return authorNameCache.computeIfAbsent(authorId, id -> {
            try {
                String url = authServiceUrl + "/auth/profile/" + id;
                var response = restTemplate.getForEntity(url, Map.class);
                if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                    Object displayName = response.getBody().get("displayName");
                    Object fullName = response.getBody().get("fullName");
                    if (displayName != null && !displayName.toString().isEmpty()) {
                        return displayName.toString();
                    }
                    if (fullName != null && !fullName.toString().isEmpty()) {
                        return fullName.toString();
                    }
                }
            } catch (Exception e) {
                // Fall through to fallback
            }
            return "User #" + id;
        });
    }

    // ── CRUD ──────────────────────────────────────────────────────

    @Override
    public CommentResponse addComment(Integer postId, Integer authorId,
                                      AddCommentRequest request) {
        if (request.getParentCommentId() != null) {
            Comment parent = getOrThrow(request.getParentCommentId());
            if (!parent.getPostId().equals(postId)) {
                throw new CommentException("Parent comment belongs to different post.");
            }
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .status(CommentStatus.APPROVED)
                .build();

        Comment saved = commentRepository.save(comment);
        publishCommentEvent(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponse> getCommentById(Integer commentId) {
        return commentRepository.findByCommentId(commentId).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentThread> getThreadedCommentsByPost(Integer postId) {
        List<Comment> topLevel = commentRepository.findTopLevelByPostId(postId);
        return topLevel.stream().map(parent -> {
            List<CommentResponse> replies = commentRepository
                    .findVisibleReplies(parent.getCommentId())
                    .stream().map(this::toResponse).toList();
            return CommentThread.builder()
                    .comment(toResponse(parent))
                    .replies(replies)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Integer postId) {
        return commentRepository.findVisibleByPostId(postId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Integer parentCommentId) {
        return commentRepository.findVisibleReplies(parentCommentId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public CommentResponse updateComment(Integer commentId, Integer requestingUserId,
                                     UpdateCommentRequest request) {
        Comment comment = getOrThrow(commentId);
        authoriseOwnership(comment, requestingUserId);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new CommentException("Cannot edit a deleted comment.");
        }
        if (StringUtils.hasText(request.getContent())) {
            comment.setContent(request.getContent());
        }
        return toResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Integer commentId, Integer requestingUserId) {
        Comment comment = getOrThrow(commentId);
        authoriseOwnership(comment, requestingUserId);

        commentRepository.softDeleteRepliesByParentId(commentId);
        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    // ── Moderation ───────────────────────────────────────────────

    @Override
    public CommentResponse approveComment(Integer commentId) {
        Comment comment = getOrThrow(commentId);
        comment.setStatus(CommentStatus.APPROVED);
        return toResponse(commentRepository.save(comment));
    }

    @Override
    public CommentResponse rejectComment(Integer commentId) {
        Comment comment = getOrThrow(commentId);
        comment.setStatus(CommentStatus.REJECTED);
        return toResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostForModeration(Integer postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getPendingComments() {
        return commentRepository.findByStatus(CommentStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCommentCount(Integer postId) {
        return commentRepository.countVisibleByPostId(postId);
    }

    // ── Like / Unlike ───────────────────────────────────────────

    @Override
    public void likeComment(Integer commentId, Integer userId) {
        Comment comment = getOrThrow(commentId);
        if (!commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            commentLikeRepository.save(CommentLike.builder()
                    .commentId(commentId)
                    .userId(userId)
                    .build());
            commentRepository.incrementLikes(commentId);
        }
    }

    @Override
    public void unlikeComment(Integer commentId, Integer userId) {
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            commentLikeRepository.deleteAndReturnCount(commentId, userId);
            commentRepository.decrementLikes(commentId);
        }
    }

    // ── Helper ───────────────────────────────────────────────

    private Comment getOrThrow(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException("Comment not found: " + commentId));
    }

    private void authoriseOwnership(Comment comment, Integer requestingUserId) {
        if (!comment.getAuthorId().equals(requestingUserId)) {
            throw new CommentException("Access denied: you are not the author of this comment.");
        }
    }

    private void publishCommentEvent(Comment comment) {
        try {
            Map<String, Object> event = Map.of(
                    "commentId",       comment.getCommentId(),
                    "postId",          comment.getPostId(),
                    "authorId",        comment.getAuthorId(),
                    "parentCommentId", comment.getParentCommentId() != null
                                       ? comment.getParentCommentId() : "null",
                    "type",            comment.getParentCommentId() != null
                                       ? "COMMENT_REPLY" : "NEW_COMMENT"
            );
            rabbitTemplate.convertAndSend(exchange, commentAddedKey, event);
        } catch (Exception e) {
            // Non-critical — log and continue
        }
    }

    // ── Mapping ───────────────────────────────────────────────────

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .commentId(c.getCommentId())
                .postId(c.getPostId())
                .authorId(c.getAuthorId())
                .authorName(getAuthorName(c.getAuthorId()))
                .parentCommentId(c.getParentCommentId())
                .content(c.getStatus() == CommentStatus.DELETED
                        ? "[This comment has been deleted]"
                        : c.getContent())
                .likesCount(c.getLikesCount())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}