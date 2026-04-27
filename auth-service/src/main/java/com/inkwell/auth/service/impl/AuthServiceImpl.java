package com.inkwell.auth.service.impl;

import com.inkwell.auth.config.JwtUtil;
import com.inkwell.auth.dto.AuthDtos.*;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.entity.User.Provider;
import com.inkwell.auth.entity.User.Role;
import com.inkwell.auth.exception.AuthException;
import com.inkwell.auth.repository.UserRepository;
import com.inkwell.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    // ── Registration ─────────────────────────────────────────────

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(resolveRegistrationRole(request.getRole()))
                .provider(Provider.LOCAL)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    // ── Login ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new AuthException("Account is deactivated. Please contact support.");
        }
        if (user.getProvider() != Provider.LOCAL || user.getPasswordHash() == null) {
            throw new AuthException("This account uses OAuth2 login (Google/GitHub).");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    // ── Token ────────────────────────────────────────────────────

    @Override
    public void logout(String token) {
        // Stateless JWT: client discards token.
        // Extend here to add token to a Redis denylist if needed.
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public String refreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthException("Invalid or expired token");
        }
        String email  = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));
        return jwtUtil.generateToken(email, user.getRole().name(), user.getUserId());
    }

    // ── Profile ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException("User not found with id: " + userId));
    }

    @Override
    public User updateProfile(Integer userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getBio())) {
            user.setBio(request.getBio());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = getUserById(userId);

        if (user.getProvider() != Provider.LOCAL || user.getPasswordHash() == null) {
            throw new AuthException("Cannot change password for OAuth2 accounts.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ── Search & Admin ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        return userRepository.searchByUsername(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        return userRepository.findAllByRole(Role.valueOf(role.toUpperCase()));
    }

    @Override
    public User changeUserRole(Integer userId, String role) {
        User user = getUserById(userId);
        user.setRole(Role.valueOf(role.toUpperCase()));
        return userRepository.save(user);
    }

    @Override
    public void deactivateAccount(Integer userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void reactivateAccount(Integer userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Integer userId) {
        userRepository.deleteByUserId(userId);
    }

    // ── OAuth2 ───────────────────────────────────────────────────

    @Override
    public AuthResponse handleOAuth2Login(String email, String fullName,
                                          String providerId, Provider provider,
                                          String avatarUrl) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // First time OAuth2 login — create a new user
            String baseUsername = email.split("@")[0];
            String username = resolveUniqueUsername(baseUsername);

            return User.builder()
                    .username(username)
                    .email(email)
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.READER)
                    .isActive(true)
                    .build();
        });

        // Update avatar / provider ID on subsequent logins
        user.setAvatarUrl(avatarUrl);
        user.setProviderId(providerId);
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name(), user.getUserId());

        UserDto userDto = UserDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .provider(user.getProvider())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .user(userDto)
                .build();
    }

    private String resolveUniqueUsername(String base) {
        String candidate = base;
        int attempt = 0;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + (++attempt);
        }
        return candidate;
    }

    private Role resolveRegistrationRole(String requestedRole) {
        if (!StringUtils.hasText(requestedRole)) {
            return Role.READER;
        }

        try {
            Role role = Role.valueOf(requestedRole.trim().toUpperCase());
            return role == Role.ADMIN ? Role.READER : role;
        } catch (IllegalArgumentException ex) {
            throw new AuthException("Unsupported registration role: " + requestedRole);
        }
    }
}
