package com.inkwell.category.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkwell.category.entity.Category;
import com.inkwell.category.entity.Tag;
import com.inkwell.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryResources {
	@Autowired
	CategoryService svc;

	@PostMapping("/categories")
	public ResponseEntity<Category> createCat(@RequestBody Category c) {
		return ResponseEntity.ok(svc.createCategory(c));
	}

	@GetMapping("/categories")
	public ResponseEntity<List<Category>> allCats() {
		return ResponseEntity.ok(svc.getAllCategories());
	}

	@GetMapping("/categories/{slug}")
	public ResponseEntity<Category> catBySlug(@PathVariable String slug) {
		return svc.getBySlug(slug).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/categories/{id}")
	public ResponseEntity<Category> updateCat(@PathVariable Integer id, @RequestBody Category c) {
		return ResponseEntity.ok(svc.updateCategory(id, c));
	}

	@DeleteMapping("/categories/{id}")
	public ResponseEntity<Void> deleteCat(@PathVariable Integer id) {
		svc.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/tags")
	public ResponseEntity<Tag> createTag(@RequestBody Tag t) {
		return ResponseEntity.ok(svc.createTag(t));
	}

	@GetMapping("/tags")
	public ResponseEntity<List<Tag>> allTags() {
		return ResponseEntity.ok(svc.getAllTags());
	}

	@GetMapping("/tags/trending")
	public ResponseEntity<List<Tag>> trending(@RequestParam(defaultValue = "10") int limit) {
		return ResponseEntity.ok(svc.getTrending(limit));
	}

	@DeleteMapping("/tags/{id}")
	public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
		svc.deleteTag(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/posts/{postId}/tags/{tagId}")
	public ResponseEntity<Void> addTag(@PathVariable Integer postId, @PathVariable Integer tagId) {
		svc.addTagToPost(postId, tagId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/posts/{postId}/tags/{tagId}")
	public ResponseEntity<Void> removeTag(@PathVariable Integer postId, @PathVariable Integer tagId) {
		svc.removeTagFromPost(postId, tagId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/posts/{postId}/tags")
	public ResponseEntity<List<Tag>> tagsByPost(@PathVariable Integer postId) {
		return ResponseEntity.ok(svc.getTagsByPost(postId));
	}

}
