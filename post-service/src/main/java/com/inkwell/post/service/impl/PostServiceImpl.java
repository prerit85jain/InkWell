package com.inkwell.post.service.impl;

import com.github.slugify.Slugify;
import com.inkwell.post.dto.PostDtos.*;
import com.inkwell.post.entity.Post;
import com.inkwell.post.entity.PostLike;
import com.inkwell.post.entity.Post.PostStatus;
import com.inkwell.post.exception.PostException;
import com.inkwell.post.repository.PostLikeRepository;
import com.inkwell.post.repository.PostRepository;
import com.inkwell.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    // ── OWASP HTML sanitiser — allows safe rich-text tags ─────────
    private static final PolicyFactory SANITIZER = new HtmlPolicyBuilder()
            .allowElements("p","br","b","i","u","strong","em","s","blockquote",
                           "ul","ol","li","h1","h2","h3","h4","h5","h6",
                           "a","img","code","pre","hr","table","thead","tbody",
                           "tr","th","td","span","div")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src","alt","width","height").onElements("img")
            .allowAttributes("class").globally()
            .requireRelNofollowOnLinks()
            .toFactory();

    private static final Slugify SLUGIFY = Slugify.builder().build();

    // ── CRUD ──────────────────────────────────────────────────────

    @Override
    public PostResponse createPost(Integer authorId, CreatePostRequest request) {
        String slug = generateUniqueSlug(request.getTitle());
        String sanitisedContent = sanitise(request.getContent());
        int readTime = computeReadTime(sanitisedContent);

        Post post = Post.builder()
                .authorId(authorId)
                .title(request.getTitle())
                .slug(slug)
                .content(sanitisedContent)
                .excerpt(request.getExcerpt())
                .featuredImageUrl(request.getFeaturedImageUrl())
                .status(PostStatus.DRAFT)
                .readTimeMin(readTime)
                .build();

        return toResponse(postRepository.save(post));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostResponse> getPostById(Integer postId, Integer currentUserId) {
        return postRepository.findByPostId(postId)
                .map(post -> toResponse(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "post_detail", key = "#slug")
    public Optional<PostResponse> getPostBySlug(String slug, Integer currentUserId) {
        return postRepository.findBySlug(slug)
                .map(post -> toResponse(post, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthor(Integer authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "'page_' + #pageable.pageNumber", 
               condition = "#pageable.pageNumber < 10")
    public Page<PostSummary> getPublishedPosts(Pageable pageable) {
        return postRepository.findPublishedOrderByFeaturedAndPublishedAt(pageable)
                .map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#pageable.pageNumber", condition = "#pageable.pageNumber < 10")
    public List<PostSummary> getPublishedPostsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return postRepository.findPublishedByIds(ids).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public PostResponse updatePost(Integer postId, Integer requestingUserId,
                                   UpdatePostRequest request) {
        Post post = getOrThrow(postId);
        authoriseOwnership(post, requestingUserId);

        if (StringUtils.hasText(request.getTitle())) {
            post.setTitle(request.getTitle());
            // Re-slug only if currently DRAFT to avoid breaking published URLs
            if (post.getStatus() == PostStatus.DRAFT) {
                post.setSlug(generateUniqueSlug(request.getTitle()));
            }
        }
        if (request.getContent() != null) {
            String sanitised = sanitise(request.getContent());
            post.setContent(sanitised);
            post.setReadTimeMin(computeReadTime(sanitised));
        }
        if (request.getExcerpt() != null)          post.setExcerpt(request.getExcerpt());
        if (request.getFeaturedImageUrl() != null)  post.setFeaturedImageUrl(request.getFeaturedImageUrl());

        return toResponse(postRepository.save(post));
    }

    @Override
    public void deletePost(Integer postId, Integer requestingUserId) {
        Post post = getOrThrow(postId);
        authoriseOwnership(post, requestingUserId);
        postRepository.deleteByPostId(postId);
    }

    // ── Lifecycle ─────────────────────────────────────────────────

    @Override
    @Caching(evict = {
        @CacheEvict(value = "posts", allEntries = true),
        @CacheEvict(value = "post_detail", key = "#result.slug")
    })
    public PostResponse publishPost(Integer postId, Integer requestingUserId) {
        Post post = getOrThrow(postId);
        authoriseOwnership(post, requestingUserId);

        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new PostException("Post is already published.");
        }
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        return toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse unpublishPost(Integer postId, Integer requestingUserId) {
        Post post = getOrThrow(postId);
        authoriseOwnership(post, requestingUserId);
        post.setStatus(PostStatus.UNPUBLISHED);
        return toResponse(postRepository.save(post));
    }

    @Override
    public PostResponse archivePost(Integer postId, Integer requestingUserId) {
        Post post = getOrThrow(postId);
        authoriseOwnership(post, requestingUserId);
        post.setStatus(PostStatus.ARCHIVED);
        return toResponse(postRepository.save(post));
    }

    // ── Search ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PostSummary> searchPosts(String query) {
        String normalizedQuery = query == null ? "" : query.toLowerCase();
        return postRepository.searchByTitleOrContent(normalizedQuery)
                .stream().map(this::toSummary).toList();
    }

    // ── Engagement ────────────────────────────────────────────────

    @Override
    @Transactional
    public void incrementViews(Integer postId) {
        postRepository.incrementViewCount(postId);
    }

    @Override
    @CacheEvict(value = "post_detail", key = "#postId")
    public void likePost(Integer postId, Integer userId) {
        getOrThrow(postId);
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            return;
        }
        try {
            postLikeRepository.save(PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build());
            postRepository.incrementLikes(postId);
            postRepository.flush();
        } catch (DataIntegrityViolationException ignored) {
            // Duplicate like from concurrent request; treat as idempotent.
        }
    }

    @Override
    @CacheEvict(value = "post_detail", key = "#postId")
    public void unlikePost(Integer postId, Integer userId) {
        getOrThrow(postId);
        int deletedCount = postLikeRepository.deleteAndReturnCount(postId, userId);
        if (deletedCount > 0) {
            postRepository.decrementLikesBy(postId, deletedCount);
        }
    }

    // ── Admin ─────────────────────────────────────────────────────

    @Override
    public PostResponse featurePost(Integer postId, boolean featured) {
        Post post = getOrThrow(postId);
        post.setIsFeatured(featured);
        return toResponse(postRepository.save(post));
    }

    // ── Counts ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getPostCountByAuthor(Integer authorId) {
        return postRepository.countByAuthorId(authorId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPublishedCount() {
        return postRepository.countByStatus(PostStatus.PUBLISHED);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Post getOrThrow(Integer postId) {
        return postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostException("Post not found: " + postId));
    }

    private void authoriseOwnership(Post post, Integer requestingUserId) {
        if (!post.getAuthorId().equals(requestingUserId)) {
            throw new PostException("Access denied: you are not the author of this post.");
        }
    }

    private String generateUniqueSlug(String title) {
        String base = SLUGIFY.slugify(title);
        String candidate = base;
        int attempt = 0;
        while (postRepository.findBySlug(candidate).isPresent()) {
            candidate = base + "-" + (++attempt);
        }
        return candidate;
    }

    private String sanitise(String html) {
        if (html == null) return "";
        return SANITIZER.sanitize(html);
    }

    private int computeReadTime(String content) {
        if (!StringUtils.hasText(content)) return 1;
        // Strip HTML tags, count words, divide by 200 WPM
        String text = content.replaceAll("<[^>]+>", " ");
        long words = java.util.Arrays.stream(text.trim().split("\\s+"))
                .filter(w -> !w.isBlank()).count();
        return (int) Math.max(1, Math.ceil(words / 200.0));
    }

    // ── Mapping ───────────────────────────────────────────────────

    private PostResponse toResponse(Post p) {
        return toResponse(p, null);
    }

    private PostResponse toResponse(Post p, Integer currentUserId) {
        return PostResponse.builder()
                .postId(p.getPostId())
                .authorId(p.getAuthorId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .content(p.getContent())
                .excerpt(p.getExcerpt())
                .featuredImageUrl(p.getFeaturedImageUrl())
                .status(p.getStatus())
                .readTimeMin(p.getReadTimeMin())
                .viewCount(p.getViewCount())
                .likesCount(p.getLikesCount())
                .likedByCurrentUser(isLikedByCurrentUser(p.getPostId(), currentUserId))
                .isFeatured(p.getIsFeatured())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .publishedAt(p.getPublishedAt())
                .build();
    }

    private boolean isLikedByCurrentUser(Integer postId, Integer currentUserId) {
        return currentUserId != null
                && postLikeRepository.existsByPostIdAndUserId(postId, currentUserId);
    }

    private PostSummary toSummary(Post p) {
        return PostSummary.builder()
                .postId(p.getPostId())
                .authorId(p.getAuthorId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .excerpt(p.getExcerpt())
                .featuredImageUrl(p.getFeaturedImageUrl())
                .readTimeMin(p.getReadTimeMin())
                .viewCount(p.getViewCount())
                .likesCount(p.getLikesCount())
                .isFeatured(p.getIsFeatured())
                .publishedAt(p.getPublishedAt())
                .build();
    }
}
