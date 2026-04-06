package com.inkwell.auth.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	@Column(unique = true, nullable = false)
	private String username;
	@Column(unique = true, nullable = false)
	private String email;
	private String passwordHash;
	private String fullName;
	@Enumerated(EnumType.STRING) @Column(nullable = false)
	private Role role;
	@Column(length = 1000)
	private String bio;
	private String avatarUrl;
	@Enumerated(EnumType.STRING)
	private Provider provider;
	@Column(nullable = false)
	private boolean isActive;
	private LocalDateTime createdAt;
	
	public enum Role{ READER, AUTHOR, ADMIN }
	public enum Provider{ LOCAL, GOOGLE, GITHUB }
	
}
