package com.inkwell.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class PostClient {

    private final RestTemplate rest;

    @Value("${app.services.post-service.url}")
    private String baseUrl;

    public PostClient(RestTemplate rest) { this.rest = rest; }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPublishedPosts(int page, int size) {
        return rest.getForObject(
                baseUrl + "/posts/published?page=" + page + "&size=" + size,
                Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPostBySlug(String slug) {
        return rest.getForObject(baseUrl + "/posts/slug/" + slug, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPostById(Integer postId) {
        return rest.getForObject(baseUrl + "/posts/" + postId, Map.class);
    }

    public List<Map<String, Object>> getPostsByAuthorEmail(String email) {
        return rest.exchange(baseUrl + "/posts/author/email?email=" + email,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> getPostsByCategory(String slug) {
        return rest.exchange(baseUrl + "/posts/category/" + slug,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> getPostsByTag(String slug) {
        return rest.exchange(baseUrl + "/posts/tag/" + slug,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> searchPosts(String query) {
        return rest.exchange(baseUrl + "/posts/search?query=" + query,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> getAllPosts() {
        return rest.exchange(baseUrl + "/posts/published",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createPost(String title, String content,
                                           String excerpt, String featuredImageUrl,
                                           String authorEmail) {
        return rest.postForObject(baseUrl + "/posts",
                Map.of("title", title,
                       "content", content != null ? content : "",
                       "excerpt", excerpt != null ? excerpt : "",
                       "featuredImageUrl", featuredImageUrl != null ? featuredImageUrl : ""),
                Map.class);
    }

    public void updatePost(Integer postId, String title, String content,
                           String excerpt, String featuredImageUrl, String authorEmail) {
        rest.put(baseUrl + "/posts/" + postId,
                Map.of("title",            title != null ? title : "",
                       "content",          content != null ? content : "",
                       "excerpt",          excerpt != null ? excerpt : "",
                       "featuredImageUrl", featuredImageUrl != null ? featuredImageUrl : ""));
    }

    public void publishPost(Integer postId, String authorEmail) {
        rest.put(baseUrl + "/posts/" + postId + "/publish", null);
    }

    public void unpublishPost(Integer postId, String authorEmail) {
        rest.put(baseUrl + "/posts/" + postId + "/unpublish", null);
    }

    public void deletePost(Integer postId, String authorEmail) {
        rest.delete(baseUrl + "/posts/" + postId);
    }

    public void adminDeletePost(Integer postId) {
        rest.delete(baseUrl + "/posts/" + postId);
    }

    public void featurePost(Integer postId, boolean featured) {
        rest.put(baseUrl + "/posts/" + postId + "/feature?featured=" + featured, null);
    }

    public void incrementView(Integer postId) {
        rest.postForLocation(baseUrl + "/posts/" + postId + "/view", null);
    }

    public void likePost(Integer postId) {
        rest.postForLocation(baseUrl + "/posts/" + postId + "/like", null);
    }

    public long getTotalPublishedCount() {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = rest.getForObject(
                baseUrl + "/posts/count/published", Map.class);
        return result != null ? ((Number) result.get("count")).longValue() : 0L;
    }
}
