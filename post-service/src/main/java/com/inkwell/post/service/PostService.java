package com.inkwell.post.service;

import java.util.List;
import com.inkwell.post.entity.Post;

public interface PostService {
	Post create(Post post);
	Post getById(Integer postId);
	Post getBySlug(String slug);
	List<Post> getByAuthor(Integer authorId);
	List<Post> getPublished();
	List<Post> search(String keyWord);
	List<Post> getAll();
	Post update(Integer postId, Post updatedPost);
	void publish(Integer postId);
	void unpublish(Integer postId);
	void delete(Integer postId);
	void incrementViews(Integer postId);
	void like(Integer postId);
	void unlike(Integer postId);
	long countByAuthor(Integer authorId);
	
}
