package com.inkwell.newsletter.resource;

import com.inkwell.newsletter.dto.NewsletterDtos.*;
import com.inkwell.newsletter.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for newsletter subscription and campaign management.
 * Base path: /newsletter
 */
@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
@Tag(name = "Newsletter", description = "Subscription lifecycle, double opt-in, campaign dispatch")
public class NewsletterResource {

    private final NewsletterService newsletterService;

    // ── Public: subscription lifecycle ───────────────────────────

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to the newsletter (triggers double opt-in email)")
    public ResponseEntity<SubscriberResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsletterService.subscribe(request));
    }

    @GetMapping("/confirm")
    @Operation(summary = "Confirm newsletter subscription via token link")
    public ResponseEntity<SubscriberResponse> confirm(@RequestParam String token) {
        return ResponseEntity.ok(newsletterService.confirmSubscription(token));
    }

    @GetMapping("/unsubscribe")
    @Operation(summary = "One-click unsubscribe via token link")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String token) {
        newsletterService.unsubscribe(token);
        return ResponseEntity.ok(Map.of("message",
                "You have been unsubscribed successfully."));
    }

    // ── Authenticated: preferences ────────────────────────────────

    @PutMapping("/preferences/{subscriberId}")
    @Operation(summary = "Update subscriber tag preferences")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SubscriberResponse> updatePreferences(
            @PathVariable Integer subscriberId,
            @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(
                newsletterService.updatePreferences(subscriberId, request));
    }

    // ── Admin: subscriber management ─────────────────────────────

    @GetMapping("/admin/subscribers")
    @Operation(summary = "Get all subscribers with status (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriberResponse>> getAllSubscribers() {
        return ResponseEntity.ok(newsletterService.getAllSubscribers());
    }

    @GetMapping("/admin/subscribers/status/{status}")
    @Operation(summary = "Get subscribers filtered by status (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriberResponse>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(newsletterService.getSubscribersByStatus(status));
    }

    @GetMapping("/admin/subscribers/email/{email}")
    @Operation(summary = "Get a subscriber by email (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriberResponse> getByEmail(@PathVariable String email) {
        return newsletterService.getSubscriberByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Admin: campaign dispatch ──────────────────────────────────

    @PostMapping("/admin/send")
    @Operation(summary = "Send a newsletter campaign to all or targeted ACTIVE subscribers (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendNewsletter(
            @Valid @RequestBody SendNewsletterRequest request) {
        newsletterService.sendNewsletter(request);
        return ResponseEntity.ok(Map.of("message",
                "Newsletter campaign dispatched asynchronously."));
    }

    // ── Admin: counts ─────────────────────────────────────────────

    @GetMapping("/admin/count")
    @Operation(summary = "Get total subscriber count (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getCount() {
        return ResponseEntity.ok(Map.of(
                "total",  newsletterService.getSubscriberCount(),
                "active", newsletterService.getActiveSubscriberCount()
        ));
    }

    // ── Internal: new-post notification (called by post-service) ──

    @PostMapping("/internal/post-notification")
    @Operation(summary = "Trigger new-post email to all ACTIVE subscribers (inter-service)")
    public ResponseEntity<Map<String, String>> postNotification(
            @RequestBody PostPublishedEvent event) {
        newsletterService.sendPostNotification(event);
        return ResponseEntity.ok(Map.of("message",
                "Post notification dispatched asynchronously."));
    }
}
