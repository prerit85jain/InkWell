package com.inkwell.media.resource;

import com.inkwell.media.dto.MediaDtos.*;
import com.inkwell.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST API for media file management.
 * Base path: /media
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Upload, retrieve, link, and delete media files")
public class MediaResource {

    private final MediaService mediaService;

    // ── Upload ────────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file to S3 (max 10 MB — JPEG, PNG, GIF, WebP, PDF)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<MediaResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        Integer uploaderId = extractUserId(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadMedia(file, uploaderId));
    }

    // ── Retrieval ─────────────────────────────────────────────────

    @GetMapping("/{mediaId}")
    @Operation(summary = "Get media metadata by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MediaResponse> getById(@PathVariable Integer mediaId) {
        return mediaService.getMediaById(mediaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/uploader/{uploaderId}")
    @Operation(summary = "Get all active media files for a specific uploader")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<MediaResponse>> getByUploader(
            @PathVariable Integer uploaderId) {
        return ResponseEntity.ok(mediaService.getMediaByUploader(uploaderId));
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get all media files linked to a post (public)")
    public ResponseEntity<List<MediaResponse>> getByPost(@PathVariable Integer postId) {
        return ResponseEntity.ok(mediaService.getMediaByPost(postId));
    }

    @GetMapping("/count/uploader/{uploaderId}")
    @Operation(summary = "Get media file count for an uploader")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Long>> countByUploader(
            @PathVariable Integer uploaderId) {
        return ResponseEntity.ok(
                Map.of("count", mediaService.getMediaCountByUploader(uploaderId)));
    }

    // ── Update alt text ───────────────────────────────────────────

    @PutMapping("/{mediaId}/alt-text")
    @Operation(summary = "Update alt text for accessibility")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MediaResponse> updateAltText(
            @PathVariable Integer mediaId,
            @Valid @RequestBody UpdateAltTextRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                mediaService.updateAltText(mediaId, extractUserId(auth), request));
    }

    // ── Link / unlink ─────────────────────────────────────────────

    @PutMapping("/{mediaId}/link")
    @Operation(summary = "Link a media file to a post")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<MediaResponse> linkToPost(
            @PathVariable Integer mediaId,
            @RequestBody LinkPostRequest request,
            Authentication auth) {
        return ResponseEntity.ok(
                mediaService.linkToPost(mediaId, request.getPostId(), extractUserId(auth)));
    }

    @PutMapping("/{mediaId}/unlink")
    @Operation(summary = "Unlink a media file from its post")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('AUTHOR','ADMIN')")
    public ResponseEntity<Map<String, String>> unlinkFromPost(
            @PathVariable Integer mediaId,
            Authentication auth) {
        mediaService.unlinkFromPost(mediaId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Media unlinked from post"));
    }

    // ── Delete ────────────────────────────────────────────────────

    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Soft-delete a media file and remove from S3")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Integer mediaId,
            Authentication auth) {
        mediaService.deleteMedia(mediaId, extractUserId(auth));
        return ResponseEntity.ok(Map.of("message", "Media deleted successfully"));
    }

    // ── Admin ─────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @Operation(summary = "Get all active media across the platform (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MediaResponse>> getAllMedia() {
        return ResponseEntity.ok(mediaService.getAllMedia());
    }

    @DeleteMapping("/admin/cleanup")
    @Operation(summary = "Hard-delete all soft-deleted media records (Admin / scheduled)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanup() {
        mediaService.cleanupDeleted();
        return ResponseEntity.ok(Map.of("message", "Cleanup complete"));
    }

    // ── Inter-service: unlink all media when a post is deleted ────

    @DeleteMapping("/internal/post/{postId}/unlink")
    @Operation(summary = "Unlink all media for a deleted post (inter-service)")
    public ResponseEntity<Map<String, String>> unlinkAllByPost(@PathVariable Integer postId) {
        mediaService.unlinkAllByPost(postId);
        return ResponseEntity.ok(Map.of("message", "All media unlinked from post " + postId));
    }

    // ── Helper ───────────────────────────────────────────────────

    private Integer extractUserId(Authentication auth) {
        Object creds = auth.getCredentials();
        if (creds instanceof Integer) return (Integer) creds;
        throw new IllegalStateException("Could not extract userId from JWT");
    }
}
