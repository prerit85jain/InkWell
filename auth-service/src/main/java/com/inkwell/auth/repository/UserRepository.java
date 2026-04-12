package com.inkwell.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inkwell.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{

	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	Optional<User> findByUserId(Integer id);
	boolean existsByEmail(String email);
	boolean existsByUsername(String username);
	List<User> findAllByRole(String role);
	List<User> findByUsernameContainingOrFullNameContaining(String username, String fullname);
	boolean deleteByUserId(Integer id);
	
}
