package com.inkwell.newsletter.service.impl;

import com.inkwell.newsletter.dto.NewsletterDtos.*;
import com.inkwell.newsletter.entity.Subscriber;
import com.inkwell.newsletter.entity.Subscriber.SubscriberStatus;
import com.inkwell.newsletter.exception.NewsletterException;
import com.inkwell.newsletter.repository.SubscriberRepository;
import com.inkwell.newsletter.service.NewsletterService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NewsletterServiceImpl implements NewsletterService {

    private final SubscriberRepository subscriberRepository;
    private final JavaMailSender       mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Subscription lifecycle ────────────────────────────────────

    @Override
    public SubscriberResponse subscribe(SubscribeRequest request) {
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            Subscriber existing = subscriberRepository.findByEmail(request.getEmail()).get();
            if (existing.getStatus() == SubscriberStatus.ACTIVE) {
                throw new NewsletterException("Email is already subscribed and active.");
            }
            if (existing.getStatus() == SubscriberStatus.PENDING) {
                existing.setToken(UUID.randomUUID().toString());
                existing.setTokenExpiresAt(LocalDateTime.now().plusHours(24));
                Subscriber saved = subscriberRepository.save(existing);
                sendConfirmationEmail(saved);
                return toResponse(saved);
            }
            if (existing.getStatus() == SubscriberStatus.UNSUBSCRIBED) {
                existing.setStatus(SubscriberStatus.PENDING);
                existing.setToken(UUID.randomUUID().toString());
                existing.setTokenExpiresAt(LocalDateTime.now().plusHours(24));
                existing.setUnsubscribedAt(null);
                Subscriber saved = subscriberRepository.save(existing);
                sendConfirmationEmail(saved);
                return toResponse(saved);
            }
        }

        Subscriber subscriber = Subscriber.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .userId(request.getUserId())
                .preferences(request.getPreferences())
                .status(SubscriberStatus.PENDING)
                .token(UUID.randomUUID().toString())
                .build();

        Subscriber saved = subscriberRepository.save(subscriber);
        sendConfirmationEmail(saved);
        return toResponse(saved);
    }

    @Override
    public SubscriberResponse confirmSubscription(String token) {
        Subscriber subscriber = subscriberRepository.findByToken(token)
                .orElseThrow(() -> new NewsletterException("Invalid or expired confirmation token."));

        if (subscriber.getStatus() == SubscriberStatus.ACTIVE) {
            return toResponse(subscriber);
        }

        if (subscriber.getStatus() == SubscriberStatus.UNSUBSCRIBED) {
            throw new NewsletterException("This subscription has been unsubscribed. Please subscribe again.");
        }

        if (subscriber.getTokenExpiresAt() != null && 
            LocalDateTime.now().isAfter(subscriber.getTokenExpiresAt())) {
            throw new NewsletterException("Confirmation token has expired. Please request a new confirmation email.");
        }

        subscriber.setStatus(SubscriberStatus.ACTIVE);
        subscriber.setConfirmedAt(LocalDateTime.now());
        subscriber.setToken(null);
        subscriber.setTokenExpiresAt(null);
        Subscriber saved = subscriberRepository.save(subscriber);
        sendWelcomeEmail(saved);
        return toResponse(saved);
    }

    @Override
    public void unsubscribe(String token) {
        Subscriber subscriber = subscriberRepository.findByToken(token)
                .orElseThrow(() -> new NewsletterException("Invalid unsubscribe token."));

        subscriber.setStatus(SubscriberStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    // ── Retrieval ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriberResponse> getSubscriberByEmail(String email) {
        return subscriberRepository.findByEmail(email).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriberResponse> getAllSubscribers() {
        return subscriberRepository.findAllByOrderBySubscribedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriberResponse> getSubscribersByStatus(String status) {
        return subscriberRepository.findByStatus(SubscriberStatus.valueOf(status.toUpperCase()))
                .stream().map(this::toResponse).toList();
    }

    // ── Campaign dispatch ─────────────────────────────────────────

    @Override
    @Async
    public void sendNewsletter(SendNewsletterRequest request) {
        List<Subscriber> targets;

        if (request.getTargetSubscriberIds() != null
                && !request.getTargetSubscriberIds().isEmpty()) {
            targets = subscriberRepository.findAllById(request.getTargetSubscriberIds())
                    .stream()
                    .filter(s -> s.getStatus() == SubscriberStatus.ACTIVE)
                    .toList();
        } else {
            targets = subscriberRepository.findByStatus(SubscriberStatus.ACTIVE);
        }

        for (Subscriber s : targets) {
            try {
                String unsubscribeUrl = baseUrl + "/newsletter/unsubscribe?token=" + s.getToken();
                String body = request.getBody()
                        + "<br><br><small><a href='" + unsubscribeUrl
                        + "'>Unsubscribe</a></small>";
                sendEmail(s.getEmail(), request.getSubject(), body);
            } catch (Exception e) {
                log.warn("Failed to send newsletter to {}: {}", s.getEmail(), e.getMessage());
            }
        }
    }

    @Override
    @Async
    public void sendPostNotification(PostPublishedEvent event) {
        List<Subscriber> active = subscriberRepository.findByStatus(SubscriberStatus.ACTIVE);
        String subject = "New Post: " + event.getPostTitle();

        for (Subscriber s : active) {
            try {
                String postUrl        = baseUrl + "/blog/" + event.getPostSlug();
                String unsubscribeUrl = baseUrl + "/newsletter/unsubscribe?token=" + s.getToken();
                String body = "<h2><a href='" + postUrl + "'>" + event.getPostTitle() + "</a></h2>"
                        + "<p>By " + event.getAuthorName() + "</p>"
                        + "<p>" + event.getExcerpt() + "</p>"
                        + "<p><a href='" + postUrl + "'>Read more →</a></p>"
                        + "<br><small><a href='" + unsubscribeUrl + "'>Unsubscribe</a></small>";
                sendEmail(s.getEmail(), subject, body);
            } catch (Exception e) {
                log.warn("Failed to send post notification to {}: {}", s.getEmail(), e.getMessage());
            }
        }
    }

    // ── Preferences ───────────────────────────────────────────────

    @Override
    public SubscriberResponse updatePreferences(Integer subscriberId,
                                                 UpdatePreferencesRequest request) {
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterException("Subscriber not found: " + subscriberId));
        subscriber.setPreferences(request.getPreferences());
        return toResponse(subscriberRepository.save(subscriber));
    }

    // ── Counts ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getSubscriberCount() {
        return subscriberRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveSubscriberCount() {
        return subscriberRepository.countByStatus(SubscriberStatus.ACTIVE);
    }

    // ── Email senders ────────────────────────────────────────────

        private void sendConfirmationEmail(Subscriber subscriber) {
        String confirmUrl = baseUrl + "/newsletter/confirm?token=" + subscriber.getToken();
        String subject    = "Confirm your InkWell subscription";
        String body = "<h2>Welcome to InkWell!</h2>"
                + "<p>Please confirm your subscription by clicking the link below:</p>"
                + "<p><a href='" + confirmUrl + "'>Confirm Subscription</a></p>"
                + "<p>This link expires in 24 hours.</p>"
                + "<p>If you did not request this, you can safely ignore this email.</p>";
        sendEmail(subscriber.getEmail(), subject, body);
    }

        private void sendWelcomeEmail(Subscriber subscriber) {
        String unsubscribeUrl = baseUrl + "/newsletter/unsubscribe?token=" + subscriber.getToken();
        String subject        = "Welcome to InkWell!";
        String body = "<h2>You're subscribed!</h2>"
                + "<p>Hi " + (subscriber.getFullName() != null
                              ? subscriber.getFullName() : "there") + ",</p>"
                + "<p>You'll now receive updates whenever new posts are published.</p>"
                + "<br><small><a href='" + unsubscribeUrl + "'>Unsubscribe</a></small>";
        sendEmail(subscriber.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Email send failed to {}: {}", to, e.getMessage());
        }
    }

    // ── Mapping ──────────────────────────────────────────────────

    private SubscriberResponse toResponse(Subscriber s) {
        return SubscriberResponse.builder()
                .subscriberId(s.getSubscriberId())
                .email(s.getEmail())
                .userId(s.getUserId())
                .fullName(s.getFullName())
                .status(s.getStatus())
                .preferences(s.getPreferences())
                .subscribedAt(s.getSubscribedAt())
                .confirmedAt(s.getConfirmedAt())
                .unsubscribedAt(s.getUnsubscribedAt())
                .build();
    }
}
