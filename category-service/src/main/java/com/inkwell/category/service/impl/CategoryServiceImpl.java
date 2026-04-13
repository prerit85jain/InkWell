package com.inkwell.category.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.inkwell.category.entity.Category;
import com.inkwell.category.entity.PostTag;
import com.inkwell.category.entity.Tag;
import com.inkwell.category.repository.CategoryRepository;
import com.inkwell.category.repository.PostTagRepository;
import com.inkwell.category.repository.TagRepository;
import com.inkwell.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
	@Autowired
	CategoryRepository categoryRepo;
	@Autowired
	TagRepository tagRepo;
	@Autowired
	PostTagRepository postTagRepo;
	
	@Override
	public Category createCategory(Category category) {
		category.setCreatedAt(LocalDate.now());
		category.setPostCount(0);
        category.setSlug(toSlug(category.getName()));
        return categoryRepo.save(category);
	}

	@Override
	public List<Category> getAllCategories() {
		return categoryRepo.findAll();
	}

	@Override
	public Optional<Category> getBySlug(String slug) {
		return categoryRepo.findBySlug(slug);
	}

	@Override
	public Category updateCategory(Integer id, Category u) {
		Category c=categoryRepo.findById(id).orElseThrow();
        c.setName(u.getName());
        c.setDescription(u.getDescription());
        return categoryRepo.save(c);
	}

	@Override
	public void deleteCategory(Integer id) {
		categoryRepo.deleteById(id);
	}

	@Override
	public Tag createTag(Tag t) {
		return tagRepo.findByName(t.getName()).orElseGet(()->{
            t.setSlug(toSlug(t.getName()));
            t.setCreatedAt(LocalDate.now());
            t.setPostCount(0);
            return tagRepo.save(t);
        });
	}

	@Override
	public List<Tag> getAllTags() {
		return tagRepo.findAll();
	}

	@Override
	public List<Tag> getTrending(int limit) {
		return tagRepo.findByOrderByPostCountDesc(PageRequest.of(0, limit));
	}

	@Override
	public void deleteTag(Integer id) {
		tagRepo.deleteById(id);
	}

	@Override
	public void addTagToPost(Integer postId, Integer tagId) {
		if(postTagRepo.findByPostIdAndTagId(postId,tagId).isEmpty()) {
            postTagRepo.save(PostTag.builder().postId(postId).tagId(tagId).build());
            tagRepo.findById(tagId).ifPresent(t->{t.setPostCount(t.getPostCount()+1);tagRepo.save(t);});
        }
	}

	@Override
	public void removeTagFromPost(Integer postId, Integer tagId) {
		postTagRepo.findByPostIdAndTagId(postId,tagId).ifPresent(pt->{
            postTagRepo.delete(pt);
            tagRepo.findById(tagId).ifPresent(t->{t.setPostCount(Math.max(0,t.getPostCount()-1));
            tagRepo.save(t);
            });
        });
	}

	@Override
	public List<Tag> getTagsByPost(Integer postId) {
		return postTagRepo.findByPostId(postId).stream()
	            .map(pt->tagRepo.findById(pt.getTagId()).orElse(null))
	            .filter(t->t!=null).collect(Collectors.toList());
	}
	
	private String toSlug(String s) {
		return s.toLowerCase().replaceAll("[^a-z0-9\\s-]","").trim().replaceAll("\\s+","-");
	}
}
