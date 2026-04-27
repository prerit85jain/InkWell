package com.inkwell.media.repository;

import com.inkwell.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Integer> {

    Optional<Media> findByMediaId(Integer mediaId);
    List<Media> findByUploaderIdAndIsDeletedFalseOrderByUploadedAtDesc(Integer uploaderId);
    List<Media> findByLinkedPostIdAndIsDeletedFalse(Integer linkedPostId);
    List<Media> findByMimeTypeContainingAndIsDeletedFalse(String mimeType);
    List<Media> findByIsDeletedFalseOrderByUploadedAtDesc();
    List<Media> findByIsDeletedTrue();
    long countByUploaderIdAndIsDeletedFalse(Integer uploaderId);

    @Transactional
    @Modifying
    @Query("UPDATE Media m SET m.isDeleted = true WHERE m.mediaId = :id")
    void softDeleteById(@Param("id") Integer mediaId);

    @Transactional
    @Modifying
    @Query("UPDATE Media m SET m.linkedPostId = null WHERE m.linkedPostId = :postId")
    void unlinkByPostId(@Param("postId") Integer postId);

    @Transactional
    void deleteByMediaId(Integer mediaId);
}
