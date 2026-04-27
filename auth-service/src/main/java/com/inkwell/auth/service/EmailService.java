package com.inkwell.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from: noreply@inkwell.com}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Async
    public void sendOtpEmail(String toEmail, String otpCode, String userName) {
        if (!mailEnabled) {
            log.info("Email disabled. OTP for {}: {}", toEmail, otpCode);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your InkWell OTP Code");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "Your verification code is: %s\n\n" +
                    "This code will expire in 5 minutes.\n" +
                    "If you didn't request this, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "InkWell Team",
                    userName != null ? userName : "User",
                    otpCode
            ));

            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        if (!mailEnabled) {
            log.info("Email disabled. Welcome email skipped for {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to InkWell!");
            message.setText(String.format(
                    "Hello %s,\n\n" +
                    "Welcome to InkWell! Your account has been created successfully.\n\n" +
                    "You can now:\n" +
                    "- Browse articles\n" +
                    "- Write and publish your own posts\n" +
                    "- Engage with the community\n\n" +
                    "Best regards,\n" +
                    "InkWell Team",
                    userName
            ));

            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }
}