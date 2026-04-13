package com.inkwell.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inkwell.category.entity.PostTag;

public interface PostTagRepository extends JpaRepository<PostTag, Integer> {
	List<PostTag> findByPostId(Integer postId);
	List<PostTag> findByTagId(Integer tagId);
	Optional<PostTag> findByPostIdAndTagId(Integer postId, Integer tagId);
	void deleteByPostIdAndTagId(Integer postId, Integer tagId);
}
