package com.inkwell.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class NotificationClient {

    private final RestTemplate rest;

    @Value("${app.services.notification-service.url}")
    private String baseUrl;

    public NotificationClient(RestTemplate rest) { this.rest = rest; }

    public List<Map<String, Object>> getNotificationsForUser(String email) {
        return rest.exchange(baseUrl + "/notifications/recipient/email?email=" + email,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public void markAsRead(Integer notificationId) {
        rest.put(baseUrl + "/notifications/" + notificationId + "/read", null);
    }

    @SuppressWarnings("unchecked")
    public void broadcastNotification(String title, String message) {
        rest.postForObject(baseUrl + "/notifications/send-bulk",
                Map.of("type", "BROADCAST", "title", title, "message", message),
                Map.class);
    }
}
