package com.inkwell.notification.resource;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inkwell.notification.entity.Notification;
import com.inkwell.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationResource {
	@Autowired
	NotificationService svc;

	@PostMapping
	public ResponseEntity<Notification> send(@RequestBody Notification n) {
		return ResponseEntity.ok(svc.send(n));
	}

	@GetMapping("/recipient/{id}")
	public ResponseEntity<List<Notification>> byRecipient(@PathVariable Integer id) {
		return ResponseEntity.ok(svc.getByRecipient(id));
	}

	@PutMapping("/{id}/read")
	public ResponseEntity<Void> markRead(@PathVariable Integer id) {
		svc.markRead(id);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/recipient/{id}/read-all")
	public ResponseEntity<Void> markAllRead(@PathVariable Integer id) {
		svc.markAllRead(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/recipient/{id}/unread-count")
	public ResponseEntity<Map<String, Long>> unread(@PathVariable Integer id) {
		return ResponseEntity.ok(Map.of("count", svc.getUnreadCount(id)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		svc.delete(id);
		return ResponseEntity.noContent().build();
	}
}