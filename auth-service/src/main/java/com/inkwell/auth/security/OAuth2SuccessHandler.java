package com.inkwell.auth.security;

import com.inkwell.auth.dto.AuthDtos.AuthResponse;
import com.inkwell.auth.entity.User;
import com.inkwell.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles the OAuth2 callback after a successful Google / GitHub login.
 * Delegates to {@link AuthService#handleOAuth2Login} to find-or-create
 * the user, then redirects with the JWT as a query parameter.
 *
 * In a production setup you would set the token in an HTTP-only cookie
 * or redirect to a front-end deep-link instead of a query param.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract common attributes (Google / GitHub share these)
        String email     = oAuth2User.getAttribute("email");
        String name      = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");   // GitHub
        if (avatarUrl == null) {
            avatarUrl = oAuth2User.getAttribute("picture");          // Google
        }

        // Determine provider from registration ID stored in the OAuth2 auth details
        String registrationId = ((org.springframework.security.oauth2.client
                .authentication.OAuth2AuthenticationToken) authentication)
                .getAuthorizedClientRegistrationId();

        User.Provider provider = "github".equalsIgnoreCase(registrationId)
                ? User.Provider.GITHUB
                : User.Provider.GOOGLE;

        String providerId = oAuth2User.getName(); // unique subject from the provider

        AuthResponse authResponse = authService.handleOAuth2Login(
                email, name, providerId, provider, avatarUrl);

        // Redirect to the web layer with the token
        String redirectUrl = "/auth/oauth2/success?token=" + authResponse.getAccessToken();
        response.sendRedirect(redirectUrl);
    }
}
