package com.inkwell.newsletter.resource;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkwell.newsletter.entity.Subscriber;
import com.inkwell.newsletter.service.NewsletterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NewsletterResource {
	@Autowired
	NewsletterService svc;

	@PostMapping("/subscribe")
	public ResponseEntity<Subscriber> sub(@RequestBody Map<String, Object> b) {
		return ResponseEntity.ok(svc.subscribe((String) b.get("email"),
				b.get("userId") != null ? ((Number) b.get("userId")).intValue() : null, (String) b.get("fullName")));
	}

	@GetMapping("/confirm")
	public ResponseEntity<Map<String, String>> confirm(@RequestParam String token) {
		svc.confirmSubscription(token);
		return ResponseEntity.ok(Map.of("message", "Confirmed!"));
	}

	@GetMapping("/unsubscribe")
	public ResponseEntity<Map<String, String>> unsub(@RequestParam String token) {
		svc.unsubscribe(token);
		return ResponseEntity.ok(Map.of("message", "Unsubscribed"));
	}

	@GetMapping
	public ResponseEntity<List<Subscriber>> all() {
		return ResponseEntity.ok(svc.getAll());
	}

	@GetMapping("/count")
	public ResponseEntity<Map<String, Long>> count() {
		return ResponseEntity.ok(Map.of("active", svc.countActive()));
	}

	@PostMapping("/send")
	public ResponseEntity<Map<String, String>> send(@RequestBody Map<String, String> b) {
		svc.sendNewsletter(b.get("subject"), b.get("body"));
		return ResponseEntity.ok(Map.of("message", "Sent"));
	}
}
