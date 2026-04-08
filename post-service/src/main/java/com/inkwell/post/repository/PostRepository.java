package com.inkwell.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.inkwell.post.entity.Post;


public interface PostRepository extends JpaRepository<Post, Integer> {
	Optional<Post> findBySlug(String slug);
	List<Post> findByAuthorIdOrderByCreatedAtDesc(Integer authorId);
	List<Post> findByStatusOrderByPublishedAtDesc(Post.PostStatus status);
	Optional<Post> findByPostId(Integer postId);
	List<Post> findByTitleContainingOrContentContaining(String title, String content);
	long countByAuthorId(Integer authorId);
	@Modifying
	@Transactional
	@Query("UPDATE Post p SET p.viewCount=p.viewCount+1 WHERE p.postId=:id")
	void incrementViews(Integer id);
	@Modifying
	@Transactional
	@Query("UPDATE Post p SET p.likesCount=p.likesCount+1 WHERE p.postId=:id")
	void incrementLikes(Integer id);
	@Modifying
	@Transactional
	@Query("UPDATE Post p SET p.likesCount=p.likesCount-1 WHERE p.postId=:id AND p.likesCount>0")
	void decrementLikes(Integer id);
	
}
