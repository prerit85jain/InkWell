package com.inkwell.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web-layer security — session-based (not stateless JWT).
 * Uses Spring Security form login + OAuth2 for the browser experience.
 * The JWT is stored in the HTTP session and forwarded as a Bearer header
 * on all RestTemplate calls to downstream services.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/blog/**", "/category/**",
                                 "/tag/**", "/search", "/register",
                                 "/newsletter/subscribe", "/newsletter/confirm",
                                 "/newsletter/unsubscribe",
                                 "/css/**", "/js/**", "/images/**",
                                 "/webjars/**").permitAll()
                // Author dashboard
                .requestMatchers("/author/**").hasAnyRole("AUTHOR", "ADMIN")
                // Admin panel
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Everything else requires login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
