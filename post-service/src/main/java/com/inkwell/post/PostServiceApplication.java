package com.inkwell.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * InkWell — Post Service
 *
 * Responsibilities:
 *   - Full post lifecycle: DRAFT → PUBLISHED ↔ UNPUBLISHED → ARCHIVED
 *   - SEO slug auto-generation via Slugify
 *   - Read-time computation (200 WPM)
 *   - Atomic view-count increments (DB-level)
 *   - Like / unlike counters
 *   - OWASP HTML sanitisation of rich-text content
 *   - Full-text search across titles and content
 *   - Admin: feature / pin posts
 *
 * Runs on port 8082 (see application.properties)
 */
@SpringBootApplication
public class PostServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }
}
