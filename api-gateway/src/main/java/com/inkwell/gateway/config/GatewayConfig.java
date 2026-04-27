package com.inkwell.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${app.services.auth-service}")
    private String authServiceUrl;

    @Value("${app.services.post-service}")
    private String postServiceUrl;

    @Value("${app.services.comment-service}")
    private String commentServiceUrl;

    @Value("${app.services.category-service}")
    private String categoryServiceUrl;

    @Value("${app.services.media-service}")
    private String mediaServiceUrl;

    @Value("${app.services.newsletter-service}")
    private String newsletterServiceUrl;

    @Value("${app.services.notification-service}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri(authServiceUrl))
                .route("post-service", r -> r
                        .path("/posts/**")
                        .uri(postServiceUrl))
                .route("comment-service", r -> r
                        .path("/comments/**")
                        .uri(commentServiceUrl))
                .route("category-service", r -> r
                        .path("/categories/**")
                        .uri(categoryServiceUrl))
                .route("media-service", r -> r
                        .path("/media/**")
                        .uri(mediaServiceUrl))
                .route("newsletter-service", r -> r
                        .path("/newsletters/**")
                        .uri(newsletterServiceUrl))
                .route("notification-service", r -> r
                        .path("/notifications/**")
                        .uri(notificationServiceUrl))
                .route("swagger", r -> r
                        .path("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**")
                        .uri("forward:/noop"))
                .build();
    }
}
