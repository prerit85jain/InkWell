package com.inkwell.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.inkwell.auth.dto.AuthResponse;
import com.inkwell.auth.dto.ChangePasswordRequest;
import com.inkwell.auth.dto.LoginRequest;
import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.repository.UserRepository;
import com.inkwell.auth.security.JWTUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
	@Autowired
	UserRepository userRepo;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	JWTUtils jwtUtil;

	@Override
	public User register(RegisterRequest req) {
		if(userRepo.existsByEmail(req.getEmail())) {
			throw new RuntimeException("Email already in use");
		}
		if(userRepo.existsByUsername(req.getUsername())) {
			throw new RuntimeException("Username used");
		}
		return userRepo.save(User.builder()
				.username(req.getUsername())
				.email(req.getEmail())
				.passwordHash(encoder.encode(req.getPassword()))
				.build());
	}

	@Override
	public AuthResponse login(LoginRequest req) {
//		User user = userRepo.findByEmail(req.getEmail()).orElseThrow(()->new RuntimeException("User not found"));
		User user = getUserByEmail(req.getEmail());
		if(!user.isActive()) {
			throw new RuntimeException("Account Suspended");
		}
		if(!encoder.matches(req.getPassword(), user.getPasswordHash())) {
			throw new RuntimeException("Invalid credentials");
		}
		return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId()), user.getRole().name(), user.getUserId(), user.getUsername(), user.getEmail()); 
	}

	@Override
	public boolean validateToken(String token) {
		return jwtUtil.isValid(token);
	}

	@Override
	public User getUserByEmail(String email) {
		User user = userRepo.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
		return user;
	}

	@Override
	public User getUserById(Integer id) {
		User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));
		return user;
	}

	@Override
	public User updateProfile(Integer userId, UpdateProfileRequest req) {
		User user = getUserById(userId);
		if(req.getFullname()!=null) {user.setFullName(req.getFullname());}
		if(req.getBio()!=null) {user.setBio(req.getBio());}
		if(req.getAvatar()!=null) {user.setAvatarUrl(req.getAvatar());}
		return userRepo.save(user);
	}

	@Override
	public void changePassword(Integer userId, ChangePasswordRequest req) {
		User user = getUserById(userId);
		if(!encoder.matches(req.getOldPassword(), user.getPasswordHash())) {throw new RuntimeException("Wrong password");}
		user.setPasswordHash(encoder.encode(req.getNewPassword()));
	}

	@Override
	public List<User> searchUsers(String keyword) {
		return userRepo.findByUsernameContainingOrFullNameContaining(keyword, keyword);
	}

	@Override
	public List<User> getAllUsers() {
		return userRepo.findAll();
	}

	@Override
	public void deactivateAccount(Integer userId) {
		User user = getUserById(userId);
		user.setActive(false);
		userRepo.save(user);
	}

	@Override
	public void activateAccount(Integer userId) {
		User user = getUserById(userId);
		user.setActive(true);
		userRepo.save(user);
	}

	@Override
	public void changeRole(Integer userId, String role) {
		User user = getUserById(userId);
		user.setRole(User.Role.valueOf(role));
		userRepo.save(user);
	}

}
