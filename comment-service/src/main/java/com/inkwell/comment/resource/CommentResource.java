package com.inkwell.comment.resource;

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
import org.springframework.web.bind.annotation.RestController;

import com.inkwell.comment.entity.Comment;
import com.inkwell.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentResource {
	@Autowired
	CommentService service;

	@PostMapping
	public ResponseEntity<Comment> add(@RequestBody Comment comment) {
		return ResponseEntity.ok(service.add(comment));
	}

	@GetMapping("/post/{postId}")
	public ResponseEntity<List<Comment>> byPostId(@PathVariable Integer postId) {
		return ResponseEntity.ok(service.getByPost(postId));
	}

	@GetMapping("/{commentId}")
	public ResponseEntity<Comment> byCommentId(@PathVariable Integer commentId) {
		return service.getByCommentId(commentId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{commentId}/replies")
	public ResponseEntity<List<Comment>> replies(@PathVariable Integer commentId) {
		return ResponseEntity.ok(service.getReplies(commentId));
	}

	@PutMapping("/{commentId}")
	public ResponseEntity<Comment> update(@PathVariable Integer commentId, @RequestBody Map<String, String> body) {
		return ResponseEntity.ok(service.update(commentId, body.get("content")));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/approve")
	public ResponseEntity<Void> approve(@PathVariable Integer id) {
		service.approve(id);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/{id}/reject")
	public ResponseEntity<Void> reject(@PathVariable Integer id) {
		service.reject(id);
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

	@GetMapping("/count/{postId}")
	public ResponseEntity<Map<String, Long>> count(@PathVariable Integer postId) {
		return ResponseEntity.ok(Map.of("count", service.count(postId)));
	}

	@GetMapping("/pending")
	public ResponseEntity<List<Comment>> pending() {
		return ResponseEntity.ok(service.getPending());
	}
}
