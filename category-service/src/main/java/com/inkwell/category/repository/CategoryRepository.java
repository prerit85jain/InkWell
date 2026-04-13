package com.inkwell.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inkwell.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	Optional<Category> findBySlug(String slug);
	List<Category> findByParentCategoryId(Integer parentId);
}
