package com.inkwell.auth.service;

import java.util.List;
import com.inkwell.auth.dto.AuthResponse;
import com.inkwell.auth.dto.ChangePasswordRequest;
import com.inkwell.auth.dto.LoginRequest;
import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.entity.User;

public interface AuthService {

	User register(RegisterRequest req);
	AuthResponse login(LoginRequest req);
	// logout remain
	boolean validateToken(String token);
	// refresh token remain
	User getUserByEmail(String email);
	User getUserById(Integer id);
	User updateProfile(Integer userId, UpdateProfileRequest req);
	void changePassword(Integer userId, ChangePasswordRequest req);
	List<User> searchUsers(String keyword);
	List<User> getAllUsers();
	void deactivateAccount(Integer userId);
	void activateAccount(Integer userId);
	void changeRole(Integer userId, String role);
	
}
