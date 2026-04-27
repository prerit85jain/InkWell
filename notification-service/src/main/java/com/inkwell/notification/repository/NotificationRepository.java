package com.inkwell.notification.repository;

import com.inkwell.notification.entity.Notification;
import com.inkwell.notification.entity.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Integer recipientId, Boolean isRead);
    List<Notification> findByType(NotificationType type);
    List<Notification> findByRelatedId(Integer relatedId);
    long countByRecipientIdAndIsRead(Integer recipientId, Boolean isRead);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId")
    void markAllReadByRecipient(@Param("recipientId") Integer recipientId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :id")
    void markReadById(@Param("id") Integer notificationId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = true")
    void deleteReadByRecipient(@Param("recipientId") Integer recipientId);

    @Transactional
    void deleteByNotificationId(Integer notificationId);
}
