package com.inkwell.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Persisted in-app notification record.
 *
 * type values (NotificationType enum):
 *   NEW_COMMENT     – someone commented on the author's post
 *   COMMENT_REPLY   – someone replied to the user's comment
 *   MENTION         – the user was @mentioned in a comment
 *   NEW_POST        – a followed author published a new post
 *   LIKE            – someone liked the user's post or comment
 *   BROADCAST       – admin broadcast message
 *
 * relatedId / relatedType enable deep-linking from the notification bell:
 *   e.g. relatedType="POST", relatedId=42  →  /blog/slug-of-post-42
 *
 * actorId is the user who triggered the event (null for system notifications).
 */
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notif_recipient",      columnList = "recipient_id"),
                @Index(name = "idx_notif_recipient_read", columnList = "recipient_id, is_read"),
                @Index(name = "idx_notif_type",           columnList = "type"),
                @Index(name = "idx_notif_related",        columnList = "related_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Column(name = "recipient_id", nullable = false)
    private Integer recipientId;

    /** Null for system / broadcast notifications. */
    @Column(name = "actor_id")
    private Integer actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /** ID of the related entity (post, comment, etc.). */
    @Column(name = "related_id")
    private Integer relatedId;

    /** Entity type string for deep-link routing (e.g. "POST", "COMMENT"). */
    @Column(name = "related_type", length = 30)
    private String relatedType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Type enum ─────────────────────────────────────────────────
    public enum NotificationType {
        NEW_COMMENT,
        COMMENT_REPLY,
        MENTION,
        NEW_POST,
        LIKE,
        BROADCAST
    }
}
