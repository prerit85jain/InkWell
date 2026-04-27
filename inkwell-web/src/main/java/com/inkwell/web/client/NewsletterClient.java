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
public class NewsletterClient {

    private final RestTemplate rest;

    @Value("${app.services.newsletter-service.url}")
    private String baseUrl;

    public NewsletterClient(RestTemplate rest) { this.rest = rest; }

    @SuppressWarnings("unchecked")
    public void subscribe(String email, String fullName) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("fullName", fullName != null ? fullName : "");
        rest.postForObject(baseUrl + "/newsletter/subscribe", body, Map.class);
    }

    public List<Map<String, Object>> getAllSubscribers() {
        return rest.exchange(baseUrl + "/newsletter/admin/subscribers",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    @SuppressWarnings("unchecked")
    public void sendCampaign(String subject, String body) {
        rest.postForObject(baseUrl + "/newsletter/admin/send",
                Map.of("subject", subject, "body", body), Map.class);
    }

    public long getSubscriberCount() {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = rest.getForObject(
                baseUrl + "/newsletter/admin/count", Map.class);
        return result != null ? ((Number) result.get("total")).longValue() : 0L;
    }

    public long getActiveSubscriberCount() {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = rest.getForObject(
                baseUrl + "/newsletter/admin/count", Map.class);
        return result != null ? ((Number) result.get("active")).longValue() : 0L;
    }
}
