package com.inkwell.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * InkWell — Category & Tag Service
 *
 * Responsibilities:
 *   - Hierarchical categories (parent / child) with SEO slugs
 *   - Flat keyword tags with postCount for trending discovery
 *   - Post ↔ Category and Post ↔ Tag association management
 *   - Denormalised postCount increments / decrements on assignment
 *   - Trending tags endpoint (top N by postCount)
 *   - Cleanup of all associations when a post is deleted
 *
 * Runs on port 8084 (see application.properties)
 */
@SpringBootApplication
public class CategoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApplication.class, args);
    }
}
