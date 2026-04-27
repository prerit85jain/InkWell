package com.inkwell.notification.service;

import com.inkwell.notification.dto.NotificationDtos.*;

import java.util.List;

/**
 * Business contract for in-app and email notification management.
 */
public interface NotificationService {

    // ── Send ─────────────────────────────────────────────────────
    NotificationResponse send(SendNotificationRequest request);

    void sendBulk(BulkNotificationRequest request);

    // ── Retrieval ─────────────────────────────────────────────────
    List<NotificationResponse> getByRecipient(Integer recipientId);

    List<NotificationResponse> getUnreadByRecipient(Integer recipientId);

    List<NotificationResponse> getAll();

    // ── Read state ────────────────────────────────────────────────
    void markAsRead(Integer notificationId);

    void markAllRead(Integer recipientId);

    long getUnreadCount(Integer recipientId);

    // ── Delete ───────────────────────────────────────────────────
    void deleteNotification(Integer notificationId);

    void deleteReadNotifications(Integer recipientId);
}
