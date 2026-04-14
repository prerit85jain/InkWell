package com.inkwell.media.resource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.inkwell.media.entity.Media;
import com.inkwell.media.service.MediaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MediaResource {
	@Autowired
	MediaService svc;
	@Value("${media.upload-dir:./uploads}")
	private String uploadDir;

	@PostMapping("/upload")
	public ResponseEntity<Media> upload(@RequestParam("file") MultipartFile f, @RequestParam Integer uploaderId) {
		return ResponseEntity.ok(svc.upload(f, uploaderId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Media> byId(@PathVariable Integer id) {
		return svc.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/uploader/{uid}")
	public ResponseEntity<List<Media>> byUploader(@PathVariable Integer uid) {
		return ResponseEntity.ok(svc.getByUploader(uid));
	}

	@GetMapping("/post/{postId}")
	public ResponseEntity<List<Media>> byPost(@PathVariable Integer postId) {
		return ResponseEntity.ok(svc.getByPost(postId));
	}

	@GetMapping
	public ResponseEntity<List<Media>> all() {
		return ResponseEntity.ok(svc.getAll());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		svc.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/alt-text")
	public ResponseEntity<Media> altText(@PathVariable Integer id, @RequestBody Map<String, String> b) {
		return ResponseEntity.ok(svc.updateAltText(id, b.get("altText")));
	}

	@PutMapping("/{id}/link/{postId}")
	public ResponseEntity<Media> link(@PathVariable Integer id, @PathVariable Integer postId) {
		return ResponseEntity.ok(svc.linkToPost(id, postId));
	}

	@GetMapping("/files/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		try {
			Path file = Paths.get(uploadDir).resolve(filename);
			Resource r = new UrlResource(file.toUri());
			if (r.exists())
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline").body(r);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
