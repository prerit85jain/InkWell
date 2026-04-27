package com.inkwell.newsletter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * InkWell — Newsletter / Subscription Service
 *
 * Responsibilities:
 *   - GDPR-compliant double opt-in subscription flow
 *     (PENDING → ACTIVE → UNSUBSCRIBED)
 *   - Sends confirmation email with unique token link (24 h expiry)
 *   - Sends welcome email on confirmation
 *   - Provides one-click unsubscribe via per-subscriber token
 *   - Admin: send newsletter campaigns (all / targeted ACTIVE subscribers)
 *   - Auto-dispatches new-post notifications to ACTIVE subscribers
 *     when post-service publishes a post
 *   - Email sent via Spring JavaMailSender → AWS SES
 *   - Campaign dispatch is @Async (non-blocking)
 *
 * Runs on port 8086 (see application.properties)
 */
@SpringBootApplication
@EnableAsync
public class NewsletterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsletterServiceApplication.class, args);
    }
}
