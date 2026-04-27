package com.inkwell.newsletter.dto;

import com.inkwell.newsletter.entity.Subscriber.SubscriberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * All Newsletter-Service request / response DTOs.
 */
public class NewsletterDtos {

    // ── Subscribe request ────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubscribeRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @Size(max = 100)
        private String fullName;

        /** Optional: comma-separated tag slugs for targeted campaigns */
        private String preferences;

        /** Optional: link to a registered user account */
        private Integer userId;
    }

    // ── Subscriber response ───────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SubscriberResponse {
        private Integer          subscriberId;
        private String           email;
        private Integer          userId;
        private String           fullName;
        private SubscriberStatus status;
        private String           preferences;
        private LocalDateTime    subscribedAt;
        private LocalDateTime    confirmedAt;
        private LocalDateTime    unsubscribedAt;
    }

    // ── Send newsletter campaign request ─────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendNewsletterRequest {

        @NotBlank(message = "Subject is required")
        private String subject;

        @NotBlank(message = "Body is required")
        private String body;             // HTML content

        /**
         * Optional list of subscriberIds to target.
         * If null/empty, sends to ALL ACTIVE subscribers.
         */
        private List<Integer> targetSubscriberIds;
    }

    // ── New post notification (triggered by post-service) ─────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PostPublishedEvent {
        private Integer postId;
        private String  postTitle;
        private String  postSlug;
        private String  authorName;
        private String  excerpt;
    }

    // ── Update preferences request ────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdatePreferencesRequest {
        private String preferences;   // comma-separated tag slugs
    }
}
