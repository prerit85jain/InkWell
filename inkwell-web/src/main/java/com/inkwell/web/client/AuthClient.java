package com.inkwell.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class AuthClient {

    private final RestTemplate rest;

    @Value("${app.services.auth-service.url}")
    private String baseUrl;

    public AuthClient(RestTemplate rest) { this.rest = rest; }

    public List<Map<String, Object>> getAllUsers() {
        return rest.exchange(baseUrl + "/auth/admin/users",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public long getTotalUserCount() {
        List<Map<String, Object>> users = getAllUsers();
        return users != null ? users.size() : 0L;
    }

    public void changeUserRole(Integer userId, String role) {
        rest.put(baseUrl + "/auth/admin/users/" + userId + "/role",
                Map.of("role", role));
    }

    public void deactivateUser(Integer userId) {
        rest.put(baseUrl + "/auth/admin/users/" + userId + "/deactivate", null);
    }

    public void reactivateUser(Integer userId) {
        rest.put(baseUrl + "/auth/admin/users/" + userId + "/reactivate", null);
    }

    public void deleteUser(Integer userId) {
        rest.delete(baseUrl + "/auth/admin/users/" + userId);
    }
}
