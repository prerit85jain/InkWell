package com.inkwell.notification.service;

import java.util.List;

import com.inkwell.notification.entity.Notification;

public interface NotificationService {
    Notification send(Notification n);
    List<Notification> getByRecipient(Integer recipientId);
    void markRead(Integer id);
    void markAllRead(Integer recipientId);
    long getUnreadCount(Integer recipientId);
    void delete(Integer id);
}
