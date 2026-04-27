package com.inkwell.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * InkWell — Media Service
 *
 * Responsibilities:
 *   - Multipart file upload to AWS S3 (JPEG, PNG, GIF, WebP, PDF — max 10 MB)
 *   - Persists media metadata: filename, S3 URL, MIME type, size, alt text
 *   - Links media files to specific posts (featured image / inline media)
 *   - Soft-delete preserves referential integrity in existing post content
 *   - Scheduled cleanup hard-deletes old soft-deleted records
 *   - Admin: view and manage platform-wide media library
 *
 * Runs on port 8085 (see application.properties)
 */
@SpringBootApplication
@EnableScheduling
public class MediaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}
