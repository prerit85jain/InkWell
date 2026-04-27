package com.inkwell.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * InkWell — Web / MVC Controller Layer
 *
 * The browser-facing frontend of the InkWell platform.
 * Renders Thymeleaf views and coordinates all user interactions
 * by calling downstream REST microservices via RestTemplate clients.
 *
 * Controllers:
 *   BlogController    — public reader experience (feed, posts, comments, search)
 *   AuthorController  — author dashboard (create/edit/publish posts, media, moderation)
 *   AdminController   — admin panel (users, content, taxonomy, newsletter, analytics)
 *
 * Authentication:
 *   Session-based Spring Security with form login + OAuth2 (Google / GitHub).
 *   The JWT issued by auth-service is stored in the HTTP session and forwarded
 *   as a Bearer token on all service client calls.
 *
 * Template locations:
 *   /templates/blog/    — reader pages
 *   /templates/author/  — author dashboard pages
 *   /templates/admin/   — admin panel pages
 *   /templates/auth/    — login / register pages
 *
 * Runs on port 8080 (see application.properties)
 */
@SpringBootApplication
public class InkWellWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(InkWellWebApplication.class, args);
    }
}
