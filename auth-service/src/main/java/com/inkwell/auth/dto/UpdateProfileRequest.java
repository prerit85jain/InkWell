package com.inkwell.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

	private String fullname, bio, avatar;
}
