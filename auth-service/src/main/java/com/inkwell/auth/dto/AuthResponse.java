package com.inkwell.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

	private String token, role;
	private Integer userId;
	private String username, email;
}
