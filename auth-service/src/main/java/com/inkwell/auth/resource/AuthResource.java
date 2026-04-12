package com.inkwell.auth.resource;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkwell.auth.dto.AuthResponse;
import com.inkwell.auth.dto.ChangePasswordRequest;
import com.inkwell.auth.dto.LoginRequest;
import com.inkwell.auth.dto.RegisterRequest;
import com.inkwell.auth.dto.UpdateProfileRequest;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthResource {
	private final AuthService service;
	
	@GetMapping("/health")
	public ResponseEntity<?> health(){
		return ResponseEntity.ok(Map.of("status", "UP"));
	}
	
	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody RegisterRequest req){
		System.out.println("Register request: " + req);
		return ResponseEntity.ok(service.register(req));
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req){
		System.out.println("Login request: " + req);
		return ResponseEntity.ok(service.login(req));
	}
	
	@GetMapping("/profile/{id}")
	public ResponseEntity<User> getProfile(@PathVariable Integer id){
		return ResponseEntity.ok(service.getUserById(id));
	}
	
	@PutMapping("/profile/{id}")
	public ResponseEntity<User> update(@PathVariable Integer id, @RequestBody UpdateProfileRequest req){
		return ResponseEntity.ok(service.updateProfile(id, req));
	}
	
	@PutMapping("/password/{id}")
	public ResponseEntity<Map<String, String>> pwd(@PathVariable Integer id, @RequestBody ChangePasswordRequest req){
		service.changePassword(id, req);
		return ResponseEntity.ok(Map.of("message", "Password Updated"));
	}
	
	@GetMapping("/users")
	public ResponseEntity<List<User>> getAll(){
		return ResponseEntity.ok(service.getAllUsers());
	}
	
	@GetMapping("/users/search")
	public ResponseEntity<List<User>> search(@RequestBody String keyword){
		return ResponseEntity.ok(service.searchUsers(keyword));
	}
	
	@PutMapping("/user/{id}/role")
	public ResponseEntity<Map<String, String>> role(@PathVariable Integer id, @RequestBody Map<String, String> body){
		service.changeRole(id, body.get("role"));
		return ResponseEntity.ok(Map.of("message", "Role updated"));
	}
	
	@PutMapping("/user/{id}/deactivate")
	public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id){
		service.deactivateAccount(id);
		return ResponseEntity.ok(Map.of("message", "Deactivated"));
	}
	
	@PutMapping("user/{id}/activate")
	public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id){
		service.activateAccount(id);
		return ResponseEntity.ok(Map.of("message", "Activated"));
	}
	
	@GetMapping("/validate")
	public ResponseEntity<Map<String, Boolean>> validate(@RequestParam String token){
		return ResponseEntity.ok(Map.of("valid", service.validateToken(token)));
	}
	
	@GetMapping("/user-by-email")
	public ResponseEntity<User> byEmail(@RequestParam String email){
		return ResponseEntity.ok(service.getUserByEmail(email));
	}
}
