package com.inkwell.media.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.inkwell.media.entity.Media;
import com.inkwell.media.repository.MediaRepository;
import com.inkwell.media.service.MediaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
	@Autowired
	MediaRepository repo;

	@Value("${media.upload-dir:./uploads}")
	private String uploadDir;

	@Override
	public Media upload(MultipartFile file, Integer uploaderId) {
		try {
			Path dir = Paths.get(uploadDir);
			Files.createDirectories(dir);
			String fname = UUID.randomUUID() + "_" + file.getOriginalFilename();
			Path dest = dir.resolve(fname);
			Files.copy(file.getInputStream(), dest);
			String url = "/media/files/" + fname;
			return repo.save(Media.builder().uploaderId(uploaderId).filename(fname)
					.originalName(file.getOriginalFilename()).url(url).mimeType(file.getContentType())
					.sizeKb(file.getSize() / 1024).uploadedAt(LocalDateTime.now()).isDeleted(false).build());
		} catch (IOException e) {
			throw new RuntimeException("Upload failed: " + e.getMessage());
		}
	}

	@Override
	public Optional<Media> getById(Integer id) {
		return repo.findById(id);
	}

	@Override
	public List<Media> getByUploader(Integer uid) {
		return repo.findByUploaderIdAndIsDeleted(uid, false);
	}
	
	@Override
	public List<Media> getByPost(Integer pid) {
		return repo.findByLinkedPostId(pid);
	}

	@Override
	public List<Media> getAll() {
		return repo.findByIsDeleted(false);
	}

	@Override
	public void delete(Integer id) {
		repo.findById(id).ifPresent(m -> {
			m.setDeleted(true);
			repo.save(m);
		});
	}

	@Override
	public Media updateAltText(Integer id, String alt) {
		Media m = repo.findById(id).orElseThrow();
		m.setAltText(alt);
		return repo.save(m);
	}

	@Override
	public Media linkToPost(Integer mid, Integer pid) {
		Media m = repo.findById(mid).orElseThrow();
		m.setLinkedPostId(pid);
		return repo.save(m);
	}
}
