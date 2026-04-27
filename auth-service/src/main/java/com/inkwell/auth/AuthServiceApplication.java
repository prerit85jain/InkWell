package com.inkwell.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * InkWell — Auth / User Service
 *
 * Responsibilities:
 *   - User registration (LOCAL) and OAuth2 login (Google / GitHub)
 *   - JWT generation, validation, and refresh
 *   - Profile management (name, bio, avatar, email, password)
 *   - Admin user management (role change, suspend, delete)
 *
 * Runs on port 8081 (see application.properties)
 */
@SpringBootApplication
@EnableAsync
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
