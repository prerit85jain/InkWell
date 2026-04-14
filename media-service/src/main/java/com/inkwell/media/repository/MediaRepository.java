package com.inkwell.media.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inkwell.media.entity.Media;

public interface MediaRepository extends JpaRepository<Media, Integer> {
	List<Media> findByUploaderIdAndIsDeleted(Integer uid, boolean deleted);
    List<Media> findByLinkedPostId(Integer postId);
    List<Media> findByIsDeleted(boolean deleted);
}
