package com.inkwell.post.service;

import com.inkwell.post.dto.PostDtos.*;
import com.inkwell.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Business contract for the Post lifecycle service.
 */
public interface PostService {

    // ── CRUD ─────────────────────────────────────────────────────
    PostResponse createPost(Integer authorId, CreatePostRequest request);

    Optional<PostResponse> getPostById(Integer postId, Integer currentUserId);

    Optional<PostResponse> getPostBySlug(String slug, Integer currentUserId);

    List<PostResponse> getPostsByAuthor(Integer authorId);

    Page<PostSummary> getPublishedPosts(Pageable pageable);

    List<PostSummary> getPublishedPostsByIds(List<Integer> ids);

    PostResponse updatePost(Integer postId, Integer requestingUserId, UpdatePostRequest request);

    void deletePost(Integer postId, Integer requestingUserId);

    // ── Lifecycle ─────────────────────────────────────────────────
    PostResponse publishPost(Integer postId, Integer requestingUserId);

    PostResponse unpublishPost(Integer postId, Integer requestingUserId);

    PostResponse archivePost(Integer postId, Integer requestingUserId);

    // ── Search ───────────────────────────────────────────────────
    List<PostSummary> searchPosts(String query);

    // ── Engagement ───────────────────────────────────────────────
    void incrementViews(Integer postId);

    void likePost(Integer postId, Integer userId);

    void unlikePost(Integer postId, Integer userId);

    // ── Admin ────────────────────────────────────────────────────
    PostResponse featurePost(Integer postId, boolean featured);

    // ── Counts ───────────────────────────────────────────────────
    long getPostCountByAuthor(Integer authorId);

    long getTotalPublishedCount();
}
