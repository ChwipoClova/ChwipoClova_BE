package com.chwipoClova.token.service;

import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.common.service.JwtProviderService;
import com.chwipoClova.token.dto.TokenDto;
import com.chwipoClova.token.entity.Token;
import com.chwipoClova.token.request.TokenReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenUserService {
    private final JwtProviderService jwtProviderService;
    private final TokenService tokenService;
    public CommonResponse<?> refreshToken(String refreshToken, TokenReq tokenReq, HttpServletResponse response) {
        // 리프레시 토큰 검증
        if (Boolean.TRUE.equals(jwtProviderService.tokenValidation(refreshToken))) {
            // redis에 있는지 확인
            Token token = tokenService.findById(refreshToken);
            if (token == null) {
                log.error("Refresh token not found in Redis: {}", refreshToken);
                jwtExceptionHandler(response, HttpStatus.UNAUTHORIZED);
            } else {
                String userId = tokenReq.getUserId();
                if (StringUtils.isBlank(userId)) {
                    log.error("User ID is blank in token request: {}", tokenReq);
                    jwtExceptionHandler(response, HttpStatus.UNAUTHORIZED);
                } else {
                    String tokenUserId = token.getUserId();
                    if (!userId.equals(tokenUserId)) {
                        log.error("User ID mismatch: request user ID = {}, token user ID = {}", userId, tokenUserId);
                        jwtExceptionHandler(response, HttpStatus.UNAUTHORIZED);
                    } else {
                        // 인증 완료 새로운 토큰 발행
                        TokenDto tokenDto = jwtProviderService.createAllToken(userId);
                        // response 헤더에 Access Token / Refresh Token 넣음
                        jwtProviderService.setResponseNmtoken(response, tokenDto);
                    }
                }
            }
        } else {
            log.error("Invalid refresh token: {}", refreshToken);
            jwtExceptionHandler(response, HttpStatus.UNAUTHORIZED);
        }

        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    public void jwtExceptionHandler(HttpServletResponse response, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String json = new ObjectMapper().writeValueAsString(new CommonResponse<String>(ExceptionCode.TOKEN_NULL.getCode(), null,ExceptionCode.TOKEN_NULL.getMessage()));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
