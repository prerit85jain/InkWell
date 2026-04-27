package com.inkwell.auth.service;

import com.inkwell.auth.dto.AuthDtos.*;
import com.inkwell.auth.entity.User;

import java.util.List;

/**
 * Business contract for authentication and user management.
 */
public interface AuthService {

    // ── Registration & Login ─────────────────────────────────────

    /** Register a new local (email/password) account. Returns JWT. */
    AuthResponse register(RegisterRequest request);

    /** Authenticate with email + password. Returns JWT token. */
    AuthResponse login(LoginRequest request);

    /** Invalidate the current session / token. */
    void logout(String token);

    // ── Token ────────────────────────────────────────────────────

    /** Validate a JWT string. Returns true if valid and not expired. */
    boolean validateToken(String token);

    /** Issue a fresh access token from a valid (non-expired) token. */
    String refreshToken(String token);

    // ── Profile ──────────────────────────────────────────────────

    User getUserByEmail(String email);

    User getUserById(Integer userId);

    /** Update display name, bio, avatar URL, or email address. */
    User updateProfile(Integer userId, UpdateProfileRequest request);

    /** Change password — verifies current password before updating. */
    void changePassword(Integer userId, ChangePasswordRequest request);

    // ── Search & Admin ───────────────────────────────────────────

    List<User> searchUsers(String query);

    List<User> getAllUsers();

    List<User> getUsersByRole(String role);

    /** Change a user's role (Admin only). */
    User changeUserRole(Integer userId, String role);

    /** Soft-deactivate a user account (sets isActive = false). */
    void deactivateAccount(Integer userId);

    /** Re-activate a previously deactivated account. */
    void reactivateAccount(Integer userId);

    /** Permanently delete a user account. */
    void deleteAccount(Integer userId);

    // ── OAuth2 ───────────────────────────────────────────────────

    /**
     * Find or create a user record from an OAuth2 login.
     * Called by the OAuth2 success handler after provider callback.
     */
    AuthResponse handleOAuth2Login(String email, String fullName,
                                   String providerId, User.Provider provider,
                                   String avatarUrl);
}
