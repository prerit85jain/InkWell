package com.inkwell.web.security;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secret;

	private Key key() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	public Claims validate(String token) {
		return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
	}

	public boolean isValid(String t) {
		try {
			validate(t);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getEmail(String t) {
		return validate(t).getSubject();
	}

	public String getRole(String t) {
		return (String) validate(t).get("role");
	}

	public Integer getUserId(String t) {
		return ((Number) validate(t).get("userId")).intValue();
	}
}