package com.inkwell.notification.resource;

import com.inkwell.notification.dto.NotificationDtos.*;
import com.inkwell.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for notification management.
 * Base path: /notifications
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications, read-state, bulk broadcast")
public class NotificationResource {

    private final NotificationService notificationService;

    // ── Retrieval ─────────────────────────────────────────────────

    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get all notifications for a user (newest first)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(
            @PathVariable Integer recipientId) {
        return ResponseEntity.ok(notificationService.getByRecipient(recipientId));
    }

    @GetMapping("/recipient/{recipientId}/unread")
    @Operation(summary = "Get only unread notifications for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<NotificationResponse>> getUnread(
            @PathVariable Integer recipientId) {
        return ResponseEntity.ok(notificationService.getUnreadByRecipient(recipientId));
    }

    @GetMapping("/recipient/{recipientId}/count")
    @Operation(summary = "Get unread notification count (badge number)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Integer recipientId) {
        return ResponseEntity.ok(
                Map.of("unread", notificationService.getUnreadCount(recipientId)));
    }

    // ── Read state ────────────────────────────────────────────────

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark a single notification as read")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Integer notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @PutMapping("/recipient/{recipientId}/read-all")
    @Operation(summary = "Mark all notifications as read for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> markAllRead(
            @PathVariable Integer recipientId) {
        notificationService.markAllRead(recipientId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    // ── Delete ────────────────────────────────────────────────────

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a single notification")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Integer notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    @DeleteMapping("/recipient/{recipientId}/read")
    @Operation(summary = "Delete all read notifications for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> deleteRead(
            @PathVariable Integer recipientId) {
        notificationService.deleteReadNotifications(recipientId);
        return ResponseEntity.ok(Map.of("message", "Read notifications cleared"));
    }

    // ── Send (inter-service / Admin) ──────────────────────────────

    @PostMapping("/send")
    @Operation(summary = "Send a single in-app notification (inter-service or Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.send(request));
    }

    @PostMapping("/send-bulk")
    @Operation(summary = "Broadcast notification to multiple users (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendBulk(
            @Valid @RequestBody BulkNotificationRequest request) {
        notificationService.sendBulk(request);
        return ResponseEntity.ok(Map.of("message",
                "Bulk notification dispatched asynchronously."));
    }

    // ── Admin: view all ───────────────────────────────────────────

    @GetMapping("/admin/all")
    @Operation(summary = "Get all notifications platform-wide (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }
}
