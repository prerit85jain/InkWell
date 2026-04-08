package com.inkwell.post.resource;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.inkwell.post.entity.Post;
import com.inkwell.post.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostResource {
	@Autowired
	PostService service;

	@PostMapping
	public ResponseEntity<Post> create(Post post) {
		return ResponseEntity.ok(service.create(post));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Post> getById(@PathVariable Integer postId) {
		return ResponseEntity.ok(service.getById(postId));
	}

	@GetMapping("/slug/{slug}")
	public ResponseEntity<Post> getBySlug(@PathVariable String slug) {
		return ResponseEntity.ok(service.getBySlug(slug));
	}

	@GetMapping("/auther/{auther}")
	public ResponseEntity<List<Post>> getByAuthor(@PathVariable Integer authorId) {
		return ResponseEntity.ok(service.getByAuthor(authorId));
	}

	@GetMapping("/published")
	public ResponseEntity<List<Post>> published() {
		return ResponseEntity.ok(service.getPublished());
	}

	@GetMapping("/search")
	public ResponseEntity<List<Post>> search(@RequestParam String keyword) {
		return ResponseEntity.ok(service.search(keyword));
	}

	@GetMapping
	public ResponseEntity<List<Post>> getAll() {
		return ResponseEntity.ok(service.getAll());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Post> update(@PathVariable Integer id, @RequestBody Post post) {
		return ResponseEntity.ok(service.update(id, post));
	}

	@PutMapping("/{id}/publish")
	public ResponseEntity<Map<String, String>> publish(@PathVariable Integer id) {
		service.publish(id);
		return ResponseEntity.ok(Map.of("message", "published"));
	}

	@PutMapping("/{id}/unpublished")
	public ResponseEntity<Map<String, String>> unpublished(@PathVariable Integer id) {
		service.unpublish(id);
		return ResponseEntity.ok(Map.of("message", "published"));
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/view")
	public ResponseEntity<Void> view(@PathVariable Integer id) {
		service.incrementViews(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/like")
	public ResponseEntity<Void> like(@PathVariable Integer id) {
		service.like(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/unlike")
	public ResponseEntity<Void> unlike(@PathVariable Integer id) {
		service.unlike(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/count/{authorId}")
	public ResponseEntity<Map<String, Long>> count(@PathVariable Integer authorId) {
		return ResponseEntity.ok(Map.of("count", service.countByAuthor(authorId)));
	}
}
