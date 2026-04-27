package com.inkwell.media.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * All Media-Service request / response DTOs.
 */
public class MediaDtos {

    // ── Upload response (returned after successful S3 upload) ────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MediaResponse {
        private Integer       mediaId;
        private Integer       uploaderId;
        private String        filename;
        private String        originalName;
        private String        url;
        private String        mimeType;
        private Long          sizeKb;
        private String        altText;
        private Integer       linkedPostId;
        private Boolean       isDeleted;
        private LocalDateTime uploadedAt;
    }

    // ── Alt text update ──────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateAltTextRequest {
        @Size(max = 500, message = "Alt text must be 500 characters or fewer")
        private String altText;
    }

    // ── Link / unlink request ─────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LinkPostRequest {
        private Integer postId;
    }
}
