package com.inkwell.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {

	private String email, password;
}
