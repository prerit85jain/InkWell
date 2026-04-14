package com.inkwell.notification.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inkwell.notification.entity.Notification;
import com.inkwell.notification.repository.NotificationRepository;
import com.inkwell.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	@Autowired
	NotificationRepository repo;

	public Notification send(Notification n) {
		n.setCreatedAt(LocalDateTime.now());
		n.setRead(false);
		return repo.save(n);
	}

	public List<Notification> getByRecipient(Integer rid) {
		return repo.findByRecipientIdOrderByCreatedAtDesc(rid);
	}

	public void markRead(Integer id) {
		repo.findById(id).ifPresent(n -> {
			n.setRead(true);
			repo.save(n);
		});
	}

	public void markAllRead(Integer rid) {
		repo.markAllRead(rid);
	}

	public long getUnreadCount(Integer rid) {
		return repo.countByRecipientIdAndIsRead(rid, false);
	}

	public void delete(Integer id) {
		repo.deleteById(id);
	}
}