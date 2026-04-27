package com.inkwell.auth.resource;

import com.inkwell.auth.config.JwtUtil;
import com.inkwell.auth.dto.AuthDtos.*;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.service.AuthService;
import com.inkwell.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for authentication and user management.
 *
 * Base path: /auth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, OTP, token, profile, and admin APIs")
public class AuthResource {

    private final AuthService authService;
    private final OtpService otpService;
    private final JwtUtil     jwtUtil;
    private final ModelMapper modelMapper;

    // ── Registration & Login ─────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP to email for verification")
    public ResponseEntity<OtpResponse> sendOtp(
            @Valid @RequestBody OtpRequest request) {
        otpService.generateAndSendOtp(request.getEmail());
        return ResponseEntity.ok(OtpResponse.builder()
                .message("OTP sent successfully")
                .email(request.getEmail())
                .expiresIn(300)
                .build());
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and get JWT token")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(otpService.verifyOtpAndLogin(
                request.getEmail(), request.getOtp()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (invalidate token client-side)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ── Token ────────────────────────────────────────────────────

    @GetMapping("/refresh")
    @Operation(summary = "Refresh the access token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String newToken = authService.refreshToken(token);
        return ResponseEntity.ok(Map.of("accessToken", newToken, "tokenType", "Bearer"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate a token (inter-service use)")
    public ResponseEntity<Map<String, Boolean>> validate(
            @RequestParam String token) {
        return ResponseEntity.ok(Map.of("valid", authService.validateToken(token)));
    }

    // ── Profile ──────────────────────────────────────────────────

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDto> getProfile(@PathVariable Integer userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/profile/{userId}")
    @Operation(summary = "Update profile (fullName, bio, avatarUrl, email)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable Integer userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        User updated = authService.updateProfile(userId, request);
        return ResponseEntity.ok(toDto(updated));
    }

    @PutMapping("/password/{userId}")
    @Operation(summary = "Change account password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Integer userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    // ── Search ───────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Search users by username or full name")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(
                authService.searchUsers(query).stream().map(this::toDto).toList());
    }

    // ── Admin endpoints ──────────────────────────────────────────

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(
                authService.getAllUsers().stream().map(this::toDto).toList());
    }

    @GetMapping("/admin/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(
                authService.getUsersByRole(role).stream().map(this::toDto).toList());
    }

    @PutMapping("/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change a user's role (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserDto> changeUserRole(
            @PathVariable Integer userId,
            @Valid @RequestBody ChangeRoleRequest request) {
        User updated = authService.changeUserRole(userId, request.getRole());
        return ResponseEntity.ok(toDto(updated));
    }

    @PutMapping("/admin/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate (suspend) a user account (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Integer userId) {
        authService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "User deactivated"));
    }

    @PutMapping("/admin/users/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate a suspended account (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> reactivateUser(@PathVariable Integer userId) {
        authService.reactivateAccount(userId);
        return ResponseEntity.ok(Map.of("message", "User reactivated"));
    }

    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a user account (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer userId) {
        authService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    // ── OAuth2 redirect landing ───────────────────────────────────

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 callback landing (token delivered as query param)")
    public ResponseEntity<Map<String, String>> oauth2Success(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("accessToken", token, "tokenType", "Bearer"));
    }

    // ── Helper ───────────────────────────────────────────────────

    private UserDto toDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }
}
