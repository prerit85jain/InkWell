package com.inkwell.auth.dto;

import com.inkwell.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * All Auth-Service DTOs in one file for clarity.
 * Separate into individual files if preferred.
 */
public class AuthDtos {

    // ── Register ─────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        private String fullName;

        private String role;
    }

    // ── Login ────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // ── Auth Response (returned on login / register / refresh) ───
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UserDto user;
    }

    // ── User DTO (safe public projection) ────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private Integer userId;
        private String username;
        private String email;
        private String fullName;
        private User.Role role;
        private String bio;
        private String avatarUrl;
        private User.Provider provider;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }

    // ── Update Profile ───────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateProfileRequest {

        @Size(max = 100, message = "Full name max 100 characters")
        private String fullName;

        @Size(max = 500, message = "Bio max 500 characters")
        private String bio;

        private String avatarUrl;

        @Email(message = "Must be a valid email address")
        private String email;
    }

    // ── Change Password ──────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangePasswordRequest {

        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;
    }

    // ── Change Role (admin use) ───────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangeRoleRequest {

        @NotBlank
        private String role;   // "READER" | "AUTHOR" | "ADMIN"
    }

    // ── OTP Request ─────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OtpRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;
    }

    // ── OTP Verify ──────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OtpVerifyRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        @NotBlank(message = "OTP is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        private String otp;
    }

    // ── OTP Response ────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OtpResponse {
        private String message;
        private String email;
        private long expiresIn;
    }
}
