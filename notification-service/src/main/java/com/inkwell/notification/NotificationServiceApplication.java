package com.inkwell.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * InkWell — Notification Service
 *
 * Responsibilities:
 *   - Persists and serves in-app notifications for:
 *       NEW_COMMENT, COMMENT_REPLY, MENTION, NEW_POST, LIKE, BROADCAST
 *   - Tracks read / unread state per recipient
 *   - Exposes unread badge count endpoint
 *   - Consumes RabbitMQ events from comment-service and post-service
 *     via NotificationEventListener
 *   - Sends email notifications via Spring JavaMailSender → AWS SES
 *   - Admin: bulk broadcast notifications to all or selected users
 *   - Bulk dispatch is @Async (non-blocking)
 *
 * Runs on port 8087 (see application.properties)
 */
@SpringBootApplication
@EnableAsync
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
