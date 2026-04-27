package com.inkwell.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * InkWell — Comment Service
 *
 * Responsibilities:
 *   - Threaded two-level comment discussions on blog posts
 *   - APPROVED / PENDING / REJECTED / DELETED moderation statuses
 *   - Soft-delete preserving thread context (child replies retained)
 *   - Comment like / unlike counters
 *   - Author moderation of own posts; Admin moderation platform-wide
 *   - Publishes NEW_COMMENT / COMMENT_REPLY events to RabbitMQ
 *     for the notification-service to process
 *
 * Runs on port 8083 (see application.properties)
 */
@SpringBootApplication
public class CommentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
