package com.inkwell.auth.service;

import com.inkwell.auth.config.JwtUtil;
import com.inkwell.auth.dto.AuthDtos.AuthResponse;
import com.inkwell.auth.entity.OtpCode;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.exception.AuthException;
import com.inkwell.auth.repository.OtpRepository;
import com.inkwell.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${app.otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.rate-limit.max-requests:3}")
    private int maxOtpRequests;

    @Value("${app.otp.rate-limit.window-minutes:15}")
    private int rateLimitWindowMinutes;

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitCache = new ConcurrentHashMap<>();

    private static class RateLimitEntry {
        int requestCount;
        LocalDateTime windowStart;

        RateLimitEntry(int count, LocalDateTime start) {
            this.requestCount = count;
            this.windowStart = start;
        }
    }

    @Transactional
    public void generateAndSendOtp(String email) {
        // Check rate limit
        checkRateLimit(email);

        // Validate user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found with email: " + email));

        // Check for existing valid OTP
        Optional<OtpCode> existingOtp = otpRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email);
        
        if (existingOtp.isPresent()) {
            OtpCode otp = existingOtp.get();
            if (otp.isValid()) {
                // Check if we can resend (wait 60 seconds between requests)
                if (otp.getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                    throw new AuthException("Please wait 60 seconds before requesting a new OTP");
                }
                // Invalidate old OTP
                otp.setVerified(true);
                otpRepository.save(otp);
            }
        }

        // Generate OTP
        String otpCode = generateOtp();
        
        // Save OTP to database
        OtpCode newOtp = OtpCode.builder()
                .email(email)
                .otpCode(otpCode)
                .userId(user.getUserId())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .attempts(0)
                .verified(false)
                .build();
        
        otpRepository.save(newOtp);
        
        // Send OTP via email
        emailService.sendOtpEmail(email, otpCode, user.getFullName());

        // Update rate limit
        updateRateLimit(email);

        log.info("OTP generated and sent to email: {}", email);
    }

    private void checkRateLimit(String email) {
        RateLimitEntry entry = rateLimitCache.get(email);
        LocalDateTime now = LocalDateTime.now();

        if (entry != null && entry.windowStart.plusMinutes(rateLimitWindowMinutes).isAfter(now)) {
            if (entry.requestCount >= maxOtpRequests) {
                throw new AuthException("Too many OTP requests. Please try again later.");
            }
        }
    }

    private void updateRateLimit(String email) {
        RateLimitEntry entry = rateLimitCache.get(email);
        LocalDateTime now = LocalDateTime.now();

        if (entry == null || entry.windowStart.plusMinutes(rateLimitWindowMinutes).isBefore(now)) {
            rateLimitCache.put(email, new RateLimitEntry(1, now));
        } else {
            entry.requestCount++;
        }
    }

    @Transactional
    public AuthResponse verifyOtpAndLogin(String email, String otpCode) {
        // Find valid OTP
        OtpCode otp = otpRepository.findByEmailAndOtpCodeAndVerifiedFalse(email, otpCode)
                .orElseThrow(() -> new AuthException("Invalid OTP"));

        // Check if expired
        if (otp.isExpired()) {
            throw new AuthException("OTP has expired. Please request a new one.");
        }

        // Check attempts
        if (otp.getAttempts() >= 3) {
            otp.setVerified(true);
            otpRepository.save(otp);
            throw new AuthException("Too many failed attempts. Please request a new OTP.");
        }

        // Increment attempts
        otpRepository.incrementAttempts(otp.getOtpId());

        // Verify OTP
        otp.setVerified(true);
        otpRepository.save(otp);

        // Get user and generate token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        if (!user.getIsActive()) {
            throw new AuthException("Account is deactivated");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());

        log.info("OTP verified successfully for email: {}", email);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .user(mapToUserDto(user))
                .build();
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private com.inkwell.auth.dto.AuthDtos.UserDto mapToUserDto(User user) {
        return com.inkwell.auth.dto.AuthDtos.UserDto.builder()
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
    }
}