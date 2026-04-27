package com.inkwell.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Core user entity for InkWell.
 * Supports LOCAL (email+password) and OAuth2 (Google / GitHub) providers.
 * role field gates access: READER → Author Dashboard → Admin Panel.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ── Primary Key ──────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    // ── Identity ─────────────────────────────────────────────────
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * Stores bcrypt hash for LOCAL provider.
     * Null for OAuth2-only accounts.
     */
    @Column(name = "password_hash")
    private String passwordHash;

    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    // ── Role ─────────────────────────────────────────────────────
    /**
     * READER  – can browse, comment, subscribe.
     * AUTHOR  – can create and manage posts.
     * ADMIN   – full platform management access.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.READER;

    // ── Profile ──────────────────────────────────────────────────
    @Size(max = 500)
    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    // ── Auth Provider ────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 10)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    /**
     * OAuth2 provider's subject ID (used for account linking).
     */
    @Column(name = "provider_id")
    private String providerId;

    // ── Status ───────────────────────────────────────────────────
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ── Audit ────────────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Lifecycle hooks ──────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Enums ────────────────────────────────────────────────────
    public enum Role {
        READER, AUTHOR, ADMIN
    }

    public enum Provider {
        LOCAL, GOOGLE, GITHUB
    }
}
