package com.inkwell.auth.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtils {
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.expiry}")
	private long expiry;
	
	private Key key() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}
	public String generateToken(String email, String role, Integer userId) {
		return Jwts.builder().setSubject(email).claim("role", role).claim("userId", userId)
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+expiry))
				.signWith(key(), SignatureAlgorithm.HS256).compact();
	}
	public Claims validate(String token) {
		return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
	}
	public boolean isValid(String token) {
		try {
			validate(token);
			return true;
		}catch(Exception e) {
			return false;
		}
	}
	public String getEmail(String token) {
		return validate(token).getSubject();
	}
	public String getRole(String token) {
		return validate(token).get("role").toString();
	}
	public String getUserId(String token) {
		return validate(token).get("userId").toString();
	}
}
