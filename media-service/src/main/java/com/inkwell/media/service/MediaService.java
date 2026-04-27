package com.inkwell.media.service;

import com.inkwell.media.dto.MediaDtos.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Business contract for media file management.
 */
public interface MediaService {

    // ── Upload / retrieval ────────────────────────────────────────
    /** Upload a file to S3 and persist its metadata. */
    MediaResponse uploadMedia(MultipartFile file, Integer uploaderId);

    Optional<MediaResponse> getMediaById(Integer mediaId);

    /** All active media files for a specific uploader. */
    List<MediaResponse> getMediaByUploader(Integer uploaderId);

    /** All active media files linked to a specific post. */
    List<MediaResponse> getMediaByPost(Integer postId);

    /** All active media across the platform (Admin). */
    List<MediaResponse> getAllMedia();

    // ── Update ───────────────────────────────────────────────────
    MediaResponse updateAltText(Integer mediaId, Integer requestingUserId,
                                UpdateAltTextRequest request);

    // ── Link / unlink ─────────────────────────────────────────────
    MediaResponse linkToPost(Integer mediaId, Integer postId, Integer requestingUserId);

    void unlinkFromPost(Integer mediaId, Integer requestingUserId);

    /** Unlink all media when a post is deleted (called by post-service). */
    void unlinkAllByPost(Integer postId);

    // ── Delete ───────────────────────────────────────────────────
    /** Soft-delete a media record and remove the file from S3. */
    void deleteMedia(Integer mediaId, Integer requestingUserId);

    /** Hard-delete all soft-deleted records (Admin / scheduled cleanup). */
    void cleanupDeleted();

    // ── Count ─────────────────────────────────────────────────────
    long getMediaCountByUploader(Integer uploaderId);
}
