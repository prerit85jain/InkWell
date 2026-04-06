package com.inkwell.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
	private String username, email, password, fullname;
}
