package com.inkwell.auth.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {

	private String oldPassword, newPassword;
}
