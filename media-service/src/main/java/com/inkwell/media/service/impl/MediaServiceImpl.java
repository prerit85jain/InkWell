package com.inkwell.media.service.impl;

import com.inkwell.media.dto.MediaDtos.*;
import com.inkwell.media.entity.Media;
import com.inkwell.media.exception.MediaException;
import com.inkwell.media.repository.MediaRepository;
import com.inkwell.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final S3Client        s3Client;

    @Value("${app.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.aws.s3.base-url}")
    private String s3BaseUrl;         // CloudFront or S3 endpoint prefix

    // Allowed MIME types (images + PDF, enforced at service layer)
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    // ── Upload ────────────────────────────────────────────────────

    @Override
    public MediaResponse uploadMedia(MultipartFile file, Integer uploaderId) {
        validateFile(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
        String extension    = getExtension(originalName);
        String storedName   = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        String s3Key        = "media/" + uploaderId + "/" + storedName;

        // Upload to S3
        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(putReq, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new MediaException("Failed to upload file to S3: " + e.getMessage());
        }

        String url = s3BaseUrl + "/" + s3Key;

        Media media = Media.builder()
                .uploaderId(uploaderId)
                .filename(storedName)
                .originalName(originalName)
                .url(url)
                .mimeType(file.getContentType())
                .sizeKb(file.getSize() / 1024)
                .isDeleted(false)
                .build();

        return toResponse(mediaRepository.save(media));
    }

    // ── Retrieval ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<MediaResponse> getMediaById(Integer mediaId) {
        return mediaRepository.findByMediaId(mediaId)
                .filter(m -> !m.getIsDeleted())
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getMediaByUploader(Integer uploaderId) {
        return mediaRepository
                .findByUploaderIdAndIsDeletedFalseOrderByUploadedAtDesc(uploaderId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getMediaByPost(Integer postId) {
        return mediaRepository.findByLinkedPostIdAndIsDeletedFalse(postId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> getAllMedia() {
        return mediaRepository.findByIsDeletedFalseOrderByUploadedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    // ── Update ────────────────────────────────────────────────────

    @Override
    public MediaResponse updateAltText(Integer mediaId, Integer requestingUserId,
                                       UpdateAltTextRequest request) {
        Media media = getActiveOrThrow(mediaId);
        authorise(media, requestingUserId);
        media.setAltText(request.getAltText());
        return toResponse(mediaRepository.save(media));
    }

    // ── Link / unlink ─────────────────────────────────────────────

    @Override
    public MediaResponse linkToPost(Integer mediaId, Integer postId, Integer requestingUserId) {
        Media media = getActiveOrThrow(mediaId);
        authorise(media, requestingUserId);
        media.setLinkedPostId(postId);
        return toResponse(mediaRepository.save(media));
    }

    @Override
    public void unlinkFromPost(Integer mediaId, Integer requestingUserId) {
        Media media = getActiveOrThrow(mediaId);
        authorise(media, requestingUserId);
        media.setLinkedPostId(null);
        mediaRepository.save(media);
    }

    @Override
    public void unlinkAllByPost(Integer postId) {
        mediaRepository.unlinkByPostId(postId);
    }

    // ── Delete ────────────────────────────────────────────────────

    @Override
    public void deleteMedia(Integer mediaId, Integer requestingUserId) {
        Media media = getActiveOrThrow(mediaId);
        authorise(media, requestingUserId);

        // Delete from S3
        try {
            String s3Key = "media/" + media.getUploaderId() + "/" + media.getFilename();
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
        } catch (Exception e) {
            // Log but don't block soft-delete — S3 cleanup can be retried
        }

        // Soft-delete the DB record
        mediaRepository.softDeleteById(mediaId);
    }

    @Override
    public void cleanupDeleted() {
        List<Media> deleted = mediaRepository.findByIsDeletedTrue();
        for (Media m : deleted) {
            mediaRepository.deleteByMediaId(m.getMediaId());
        }
    }

    // ── Count ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getMediaCountByUploader(Integer uploaderId) {
        return mediaRepository.countByUploaderIdAndIsDeletedFalse(uploaderId);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Media getActiveOrThrow(Integer mediaId) {
        return mediaRepository.findByMediaId(mediaId)
                .filter(m -> !m.getIsDeleted())
                .orElseThrow(() -> new MediaException("Media not found: " + mediaId));
    }

    private void authorise(Media media, Integer requestingUserId) {
        if (!media.getUploaderId().equals(requestingUserId)) {
            throw new MediaException("Access denied: you did not upload this file.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaException("No file provided.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new MediaException("File exceeds the 10 MB size limit.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new MediaException(
                    "Unsupported file type. Allowed: JPEG, PNG, GIF, WebP, PDF.");
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1)
                ? filename.substring(dot + 1).toLowerCase()
                : "";
    }

    private MediaResponse toResponse(Media m) {
        return MediaResponse.builder()
                .mediaId(m.getMediaId())
                .uploaderId(m.getUploaderId())
                .filename(m.getFilename())
                .originalName(m.getOriginalName())
                .url(m.getUrl())
                .mimeType(m.getMimeType())
                .sizeKb(m.getSizeKb())
                .altText(m.getAltText())
                .linkedPostId(m.getLinkedPostId())
                .isDeleted(m.getIsDeleted())
                .uploadedAt(m.getUploadedAt())
                .build();
    }
}
