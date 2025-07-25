package com.chwipoClova.common.service;

import com.chwipoClova.token.dto.TokenDto;
import com.chwipoClova.token.entity.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface JwtProviderService {
    String getToken(HttpServletRequest request, String type);
    TokenDto createAllToken(String userId);
    String createToken(String id, String type);
    Boolean tokenValidation(String token);
    Token selectRefreshToken(String token);
    Authentication createAuthentication(Long id);
    String getIdFromToken(String token);
    void setToken(HttpServletResponse response, String token, String type);
    void setDelToken(HttpServletResponse response, String type);
    void deleteAllToken(HttpServletRequest request, HttpServletResponse response);
    void setResponseNmtoken(HttpServletResponse response, TokenDto tokenDto);
}
