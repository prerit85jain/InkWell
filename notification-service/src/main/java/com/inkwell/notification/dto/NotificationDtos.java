package com.inkwell.notification.dto;

import com.inkwell.notification.entity.Notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * All Notification-Service request / response DTOs.
 */
public class NotificationDtos {

    // ── Send single notification ─────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SendNotificationRequest {

        @NotNull(message = "recipientId is required")
        private Integer recipientId;

        private Integer actorId;

        @NotNull(message = "type is required")
        private NotificationType type;

        @NotBlank(message = "title is required")
        private String title;

        @NotBlank(message = "message is required")
        private String message;

        private Integer relatedId;
        private String  relatedType;
    }

    // ── Send bulk / broadcast ─────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class BulkNotificationRequest {

        @NotNull
        private NotificationType type;

        @NotBlank
        private String title;

        @NotBlank
        private String message;

        /** If null/empty → broadcast to all users in recipientIds. */
        private List<Integer> recipientIds;

        private Integer relatedId;
        private String  relatedType;
    }

    // ── Notification response ────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NotificationResponse {
        private Integer          notificationId;
        private Integer          recipientId;
        private Integer          actorId;
        private NotificationType type;
        private String           title;
        private String           message;
        private Integer          relatedId;
        private String           relatedType;
        private Boolean          isRead;
        private LocalDateTime    createdAt;
    }
}
