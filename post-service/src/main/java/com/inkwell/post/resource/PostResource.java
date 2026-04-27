package com.inkwell.post.resource;

import com.inkwell.post.dto.PostDtos.*;
import com.inkwell.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for post management.
 * Base path: /posts
 *
 * Authenticated endpoints extract the requesting userId from the JWT
 * via the credentials field populated by JwtAuthenticationFilter.
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "CRUD, lifecycle, search, and engagement APIs")
public class PostResource {

    private final PostService postService;

    // ── Public read ───────────────────────────────────────────────

    @GetMapping("/published")
    @Operation(summary = "Get paginated published post feed (newest / featured first)")
    public ResponseEntity<Page<PostSummary>> getPublished(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getPublishedPosts(pageable));
    }

    @GetMapping("/by-ids")
    @Operation(summary = "Get published posts by a list of IDs")
    public ResponseEntity<List<PostSummary>> getByIds(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(postService.getPublishedPostsByIds(ids));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get full post by SEO slug")
    public ResponseEntity<PostResponse> getBySlug(@PathVariable String slug, Authentication auth) {
        return postService.getPostBySlug(slug, extractUserIdOrNull(auth))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search across titles and content")
    public ResponseEntity<List<PostSummary>> search(@RequestParam String query) {
        return ResponseEntity.ok(postService.searchPosts(query));
    }

    // ── Authenticated read ────────────────────────────────────────

    @GetMapping("/{postId}")
    @Operation(summary = "Get post by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostResponse> getById(@PathVariable Integer postId, Authentication auth) {
        return postService.getPostById(postId, extractUserIdOrNull(auth))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get all posts by author ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<PostResponse>> getByAuthor(@PathVariable Integer authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    // ── Author: create / edit / delete ───────────────────────────

    @PostMapping
    @Operation(summary = "Create a new post (saved as DRAFT)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<PostResponse> create(
            @Valid @RequestBody CreatePostRequest request,
            Authentication auth) {
        Integer authorId = extractUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(authorId, request));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update a post")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<PostResponse> update(
            @PathVariable Integer postId,
            @RequestBody UpdatePostRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                postService.updatePost(postId, extractUserId(auth), request));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post and all associated comments")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Integer postId,
            Authentication auth) {
        postService.deletePost(postId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    // ── Lifecycle ─────────────────────────────────────────────────

    @PutMapping("/{postId}/publish")
    @Operation(summary = "Publish a draft or unpublished post")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<PostResponse> publish(
            @PathVariable Integer postId, Authentication auth) {
        return ResponseEntity.ok(postService.publishPost(postId, extractUserId(auth)));
    }

    @PutMapping("/{postId}/unpublish")
    @Operation(summary = "Unpublish a published post")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<PostResponse> unpublish(
            @PathVariable Integer postId, Authentication auth) {
        return ResponseEntity.ok(postService.unpublishPost(postId, extractUserId(auth)));
    }

    @PutMapping("/{postId}/archive")
    @Operation(summary = "Archive a post")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<PostResponse> archive(
            @PathVariable Integer postId, Authentication auth) {
        return ResponseEntity.ok(postService.archivePost(postId, extractUserId(auth)));
    }

    // ── Engagement ────────────────────────────────────────────────

    @PostMapping("/{postId}/view")
    @Operation(summary = "Increment view count (call once per unique session)")
    public ResponseEntity<Map<String, Object>> incrementView(@PathVariable Integer postId) {
        postService.incrementViews(postId);
        var updatedPost = postService.getPostById(postId, null);
        return ResponseEntity.ok(Map.of(
                "viewCount", updatedPost.map(p -> p.getViewCount()).orElse(0),
                "postId", postId
        ));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "Like a post")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> like(@PathVariable Integer postId, Authentication auth) {
        postService.likePost(postId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Post liked"));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "Unlike a post")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> unlike(@PathVariable Integer postId, Authentication auth) {
        postService.unlikePost(postId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Post unliked"));
    }

    // ── Admin ─────────────────────────────────────────────────────

    @PutMapping("/{postId}/feature")
    @Operation(summary = "Pin post to top of feed (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> feature(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "true") boolean featured) {
        return ResponseEntity.ok(postService.featurePost(postId, featured));
    }

    // ── Counts ───────────────────────────────────────────────────

    @GetMapping("/count/author/{authorId}")
    @Operation(summary = "Get post count for an author")
    public ResponseEntity<Map<String, Long>> countByAuthor(@PathVariable Integer authorId) {
        return ResponseEntity.ok(Map.of("count", postService.getPostCountByAuthor(authorId)));
    }

    @GetMapping("/count/published")
    @Operation(summary = "Get total published post count")
    public ResponseEntity<Map<String, Long>> countPublished() {
        return ResponseEntity.ok(Map.of("count", postService.getTotalPublishedCount()));
    }

    // ── Helper ───────────────────────────────────────────────────

    /**
     * The JwtAuthenticationFilter stores the userId as the credentials
     * of the UsernamePasswordAuthenticationToken.
     */
    private Integer extractUserId(Authentication auth) {
        Object creds = auth.getCredentials();
        if (creds instanceof Integer) return (Integer) creds;
        throw new IllegalStateException("Could not extract userId from JWT");
    }

    private Integer extractUserIdOrNull(Authentication auth) {
        if (auth == null) return null;
        Object creds = auth.getCredentials();
        return creds instanceof Integer ? (Integer) creds : null;
    }
}
