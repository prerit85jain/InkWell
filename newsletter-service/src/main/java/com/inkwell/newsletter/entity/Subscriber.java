package com.inkwell.newsletter.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Newsletter subscriber record.
 *
 * Double opt-in flow:
 *   1. POST /newsletter/subscribe  →  status = PENDING, token generated, confirmation email sent
 *   2. GET  /newsletter/confirm?token=…  →  status = ACTIVE, welcome email sent
 *   3. GET  /newsletter/unsubscribe?token=…  →  status = UNSUBSCRIBED (one-click)
 *
 * token is a unique UUID used for both confirmation and unsubscribe links.
 * userId is nullable — non-registered visitors can subscribe by email only.
 * preferences stores comma-separated tag slugs for targeted campaigns (e.g. "java,spring").
 */
@Entity
@Table(name = "subscribers",
        indexes = {
                @Index(name = "idx_sub_email",  columnList = "email",  unique = true),
                @Index(name = "idx_sub_token",  columnList = "token",  unique = true),
                @Index(name = "idx_sub_status", columnList = "status"),
                @Index(name = "idx_sub_user",   columnList = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscriber_id")
    private Integer subscriberId;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** Nullable — links to a registered user account if they signed up while logged in. */
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private SubscriberStatus status = SubscriberStatus.PENDING;

    /** Unique UUID for confirmation and unsubscribe links. */
    @Column(name = "token", nullable = false, unique = true, length = 64)
    private String token;

    /** Comma-separated tag slugs for targeted campaigns (e.g. "java,spring,aws"). */
    @Column(name = "preferences", length = 500)
    private String preferences;

    /** Token expiry timestamp (defaults to 24 hours after subscription). */
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "subscribed_at", nullable = false, updatable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    @PrePersist
    protected void onCreate() {
        this.subscribedAt = LocalDateTime.now();
        this.tokenExpiresAt = LocalDateTime.now().plusHours(24);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Status enum ───────────────────────────────────────────────
    public enum SubscriberStatus {
        PENDING,        // awaiting double opt-in confirmation
        ACTIVE,         // confirmed and receiving emails
        UNSUBSCRIBED    // opted out via unsubscribe link
    }
}
