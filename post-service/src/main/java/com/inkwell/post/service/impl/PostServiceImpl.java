package com.inkwell.post.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.inkwell.post.entity.Post;
import com.inkwell.post.entity.Post.PostStatus;
import com.inkwell.post.repository.PostRepository;
import com.inkwell.post.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{

	@Autowired
	PostRepository postRepo;
	@Autowired
	RabbitTemplate rabbit;
	
	@Override
	@CacheEvict(value = "published-posts", allEntries = true)
	public Post create(Post post) {
		post.setSlug(uniqueSlug(post.getTitle()));
		post.setStatus(Post.PostStatus.DRAFT);
		post.setReadTimeMin(Math.max(1, wordCount(post.getContent())/200));
		post.setCreatedAt(LocalDateTime.now());
		post.setViewCount(0);
		post.setLikesCount(0);
		return postRepo.save(post);
	}

	@Override
	public Post getById(Integer postId) {
		Post post = postRepo.findByPostId(postId).orElseThrow(()->new RuntimeException("Post not found"));
		return post;
	}

	@Override
	public Post getBySlug(String slug) {
		Post post = postRepo.findBySlug(slug).orElseThrow(()-> new RuntimeException("Slug not found"));
		return post;
	}

	@Override
	public List<Post> getByAuthor(Integer authorId) {
		return postRepo.findByAuthorIdOrderByCreatedAtDesc(authorId);
	}

	@Override
	@Cacheable("published-posts")
	public List<Post> getPublished() {
		return postRepo.findByStatusOrderByPublishedAtDesc(Post.PostStatus.PUBLISHED);
	}

	@Override
	public List<Post> search(String keyWord) {
		return postRepo.findByTitleContainingOrContentContaining(keyWord, keyWord);
	}

	@Override
	public List<Post> getAll() {
		return postRepo.findAll();
	}

	@Override
	@CacheEvict(value = "published-posts", allEntries = true)
	public Post update(Integer postId, Post updatedPost) {
		Post post = getById(postId);
		post.setTitle(updatedPost.getTitle());
		post.setContent(updatedPost.getContent());
		post.setExcerpt(updatedPost.getExcerpt());
		post.setFeaturedImageUrl(updatedPost.getFeaturedImageUrl());
		post.setReadTimeMin(Math.max(1, wordCount(updatedPost.getContent())/200));
		post.setUpdatedAt(LocalDateTime.now());
		return postRepo.save(post);
	}

	@Override
	@CacheEvict(value = "published-posts", allEntries = true)
	public void publish(Integer postId) {
		Post post = getById(postId);
		post.setStatus(PostStatus.PUBLISHED);
		post.setPublishedAt(LocalDateTime.now());
		postRepo.save(post);
		try {
			rabbit.convertAndSend("inkwell.exchange", "post.published", postId.toString());
		}catch (Exception ignored) {}
	}

	@Override
	@CacheEvict(value = "published-posts", allEntries = true)
	public void unpublish(Integer postId) {
		Post post = getById(postId);
		post.setStatus(PostStatus.UNPUBLISHED);
		postRepo.save(post);
	}

	@Override
	@CacheEvict(value = "published-posts", allEntries = true)
	public void delete(Integer postId) {
		postRepo.deleteById(postId);
	}

	@Override
	public void incrementViews(Integer postId) {
		postRepo.incrementViews(postId);
	}

	@Override
	public void like(Integer postId) {
		postRepo.incrementLikes(postId);
	}

	@Override
	public void unlike(Integer postId) {
		postRepo.decrementLikes(postId);
	}

	@Override
	public long countByAuthor(Integer authorId) {
		return postRepo.countByAuthorId(authorId);
	}
	
	private String uniqueSlug(String title) {
		String base = title.toLowerCase().replaceAll("[^a-z0-9\\s-]", "").trim().replaceAll("\\s+", "-");
		String s=base;
		int i=1;
		while(postRepo.findBySlug(s).isPresent()) {
			s=base+"-"+(i++);
		}
		return s;
	}
	private int wordCount(String html) {
		if(html==null || html.isBlank()) {
			return 0;
		}
		return html.replaceAll("<[^>]+>", " ").trim().split("\\s+").length;
	}

}
