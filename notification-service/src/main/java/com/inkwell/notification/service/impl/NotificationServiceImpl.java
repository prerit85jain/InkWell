package com.inkwell.notification.service.impl;

import com.inkwell.notification.dto.NotificationDtos.*;
import com.inkwell.notification.entity.Notification;
import com.inkwell.notification.entity.Notification.NotificationType;
import com.inkwell.notification.exception.NotificationException;
import com.inkwell.notification.repository.NotificationRepository;
import com.inkwell.notification.service.NotificationService;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender         mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── Send ──────────────────────────────────────────────────────

    @Override
    public NotificationResponse send(SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .actorId(request.getActorId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedId(request.getRelatedId())
                .relatedType(request.getRelatedType())
                .isRead(false)
                .build();

        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Async
    public void sendBulk(BulkNotificationRequest request) {
        if (request.getRecipientIds() == null || request.getRecipientIds().isEmpty()) {
            log.warn("sendBulk called with no recipient IDs — nothing sent.");
            return;
        }
        for (Integer recipientId : request.getRecipientIds()) {
            try {
                Notification notification = Notification.builder()
                        .recipientId(recipientId)
                        .type(request.getType())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .relatedId(request.getRelatedId())
                        .relatedType(request.getRelatedType())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.warn("Failed to create notification for recipient {}: {}",
                        recipientId, e.getMessage());
            }
        }
    }

    // ── Retrieval ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRecipient(Integer recipientId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadByRecipient(Integer recipientId) {
        return notificationRepository
                .findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    // ── Read state ────────────────────────────────────────────────

    @Override
    public void markAsRead(Integer notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new NotificationException("Notification not found: " + notificationId);
        }
        notificationRepository.markReadById(notificationId);
    }

    @Override
    public void markAllRead(Integer recipientId) {
        notificationRepository.markAllReadByRecipient(recipientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Integer recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    // ── Delete ───────────────────────────────────────────────────

    @Override
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteByNotificationId(notificationId);
    }

    @Override
    public void deleteReadNotifications(Integer recipientId) {
        notificationRepository.deleteReadByRecipient(recipientId);
    }

    // ── Email helper ──────────────────────────────────────────────

    @Async
    public void sendEmailNotification(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Email notification failed to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Mapping ──────────────────────────────────────────────────

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .recipientId(n.getRecipientId())
                .actorId(n.getActorId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedId(n.getRelatedId())
                .relatedType(n.getRelatedType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
