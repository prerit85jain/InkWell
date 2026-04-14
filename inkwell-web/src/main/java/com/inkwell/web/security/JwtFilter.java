package com.inkwell.web.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	@Autowired
    JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if(auth!=null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if(jwtUtil.isValid(token)) {
                String role = jwtUtil.getRole(token);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_"+role));
                var authentication = new UsernamePasswordAuthenticationToken(jwtUtil.getEmail(token),null,authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(req,res);
    }
}
