package com.inkwell.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommentClient {

    private final RestTemplate rest;

    @Value("${app.services.comment-service.url}")
    private String baseUrl;

    public CommentClient(RestTemplate rest) { this.rest = rest; }

    public List<Map<String, Object>> getThreadedComments(Integer postId) {
        return rest.exchange(baseUrl + "/comments/post/" + postId + "/thread",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> getCommentsForModeration(Integer postId) {
        return rest.exchange(baseUrl + "/comments/post/" + postId + "/moderate",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public List<Map<String, Object>> getAllPendingComments() {
        return rest.exchange(baseUrl + "/comments/admin/pending",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public void addComment(Integer postId, String content,
                           Integer parentCommentId, String authorEmail) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        if (parentCommentId != null) {
            body.put("parentCommentId", parentCommentId);
        }
        rest.postForObject(baseUrl + "/comments/post/" + postId, body, Map.class);
    }

    public void deleteComment(Integer commentId, String authorEmail) {
        rest.delete(baseUrl + "/comments/" + commentId);
    }

    public void adminDeleteComment(Integer commentId) {
        rest.delete(baseUrl + "/comments/" + commentId);
    }

    public void approveComment(Integer commentId) {
        rest.put(baseUrl + "/comments/" + commentId + "/approve", null);
    }

    public void rejectComment(Integer commentId) {
        rest.put(baseUrl + "/comments/" + commentId + "/reject", null);
    }

    public void likeComment(Integer commentId) {
        rest.postForLocation(baseUrl + "/comments/" + commentId + "/like", null);
    }

    public long getCommentCount(Integer postId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = rest.getForObject(
                baseUrl + "/comments/count/" + postId, Map.class);
        return result != null ? ((Number) result.get("count")).longValue() : 0L;
    }
}
