package com.inkwell.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inkwell.category.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer> {
	Optional<Tag> findBySlug(String slug);
	Optional<Tag> findByName(String name);
	List<Tag> findByOrderByPostCountDesc(Pageable pageable);
}
