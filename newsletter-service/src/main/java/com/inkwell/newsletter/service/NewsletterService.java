package com.inkwell.newsletter.service;

import com.inkwell.newsletter.dto.NewsletterDtos.*;

import java.util.List;
import java.util.Optional;

/**
 * Business contract for newsletter subscription and campaign dispatch.
 */
public interface NewsletterService {

    // ── Subscription lifecycle ────────────────────────────────────

    /** Subscribe with double opt-in — creates PENDING record, sends confirmation email. */
    SubscriberResponse subscribe(SubscribeRequest request);

    /** Confirm subscription via token link — sets ACTIVE, sends welcome email. */
    SubscriberResponse confirmSubscription(String token);

    /** One-click unsubscribe via token link — sets UNSUBSCRIBED. */
    void unsubscribe(String token);

    // ── Retrieval ─────────────────────────────────────────────────

    Optional<SubscriberResponse> getSubscriberByEmail(String email);

    List<SubscriberResponse> getAllSubscribers();

    List<SubscriberResponse> getSubscribersByStatus(String status);

    // ── Campaign dispatch ─────────────────────────────────────────

    /** Send a newsletter campaign to all ACTIVE subscribers or a targeted subset. */
    void sendNewsletter(SendNewsletterRequest request);

    /** Auto-dispatch new-post notification to all ACTIVE subscribers. */
    void sendPostNotification(PostPublishedEvent event);

    // ── Preferences ───────────────────────────────────────────────

    SubscriberResponse updatePreferences(Integer subscriberId,
                                         UpdatePreferencesRequest request);

    // ── Counts ───────────────────────────────────────────────────

    long getSubscriberCount();

    long getActiveSubscriberCount();
}
