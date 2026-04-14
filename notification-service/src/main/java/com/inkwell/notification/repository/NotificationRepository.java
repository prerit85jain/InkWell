package com.inkwell.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.inkwell.notification.entity.Notification;

import jakarta.transaction.Transactional;

public interface NotificationRepository extends JpaRepository<Notification,Integer> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer rid);
    List<Notification> findByRecipientIdAndIsRead(Integer rid, boolean read);
    long countByRecipientIdAndIsRead(Integer rid, boolean read);
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead=true WHERE n.recipientId=:rid")
    void markAllRead(Integer rid);
}
