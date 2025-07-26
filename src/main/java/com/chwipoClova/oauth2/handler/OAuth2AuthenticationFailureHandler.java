package com.chwipoClova.oauth2.handler;

import com.chwipoClova.common.service.JwtProviderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${home.url}")
    private String homeUrl;

    @Value("${home.error}")
    private String error;

    private final JwtProviderService jwtProviderService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        jwtProviderService.deleteAllToken(request, response);
        getRedirectStrategy().sendRedirect(request, response, homeUrl+error);
    }
}
