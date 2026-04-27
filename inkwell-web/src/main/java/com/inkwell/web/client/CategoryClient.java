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
public class CategoryClient {

    private final RestTemplate rest;

    @Value("${app.services.category-service.url}")
    private String baseUrl;

    public CategoryClient(RestTemplate rest) { this.rest = rest; }

    public List<Map<String, Object>> getAllCategories() {
        return rest.exchange(baseUrl + "/categories",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCategoryBySlug(String slug) {
        return rest.getForObject(baseUrl + "/categories/" + slug, Map.class);
    }

    public List<Map<String, Object>> getAllTags() {
        return rest.exchange(baseUrl + "/tags",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getTagBySlug(String slug) {
        return rest.getForObject(baseUrl + "/tags/" + slug, Map.class);
    }

    public List<Map<String, Object>> getTrendingTags() {
        return rest.exchange(baseUrl + "/tags/trending",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public void createCategory(String name, String description, Integer parentCategoryId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description != null ? description : "");
        if (parentCategoryId != null) {
            body.put("parentCategoryId", parentCategoryId);
        }
        rest.postForObject(baseUrl + "/categories", body, Map.class);
    }

    public void deleteCategory(Integer categoryId) {
        rest.delete(baseUrl + "/categories/" + categoryId);
    }

    public void deleteTag(Integer tagId) {
        rest.delete(baseUrl + "/tags/" + tagId);
    }
}
