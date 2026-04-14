package com.inkwell.media.service;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.inkwell.media.entity.Media;

public interface MediaService {
    Media upload(MultipartFile file, Integer uploaderId);
    Optional<Media> getById(Integer id);
    List<Media> getByUploader(Integer uid);
    List<Media> getByPost(Integer postId);
    List<Media> getAll();
    void delete(Integer id);
    Media updateAltText(Integer id, String altText);
    Media linkToPost(Integer mediaId, Integer postId);
}