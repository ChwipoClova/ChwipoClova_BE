package com.chwipoClova.token.controller;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.service.JwtProviderService;
import com.chwipoClova.token.request.TokenReq;
import com.chwipoClova.token.service.TokenUserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Token", description = "토큰 API")
@RequestMapping("token")
public class TokenController {
    private final TokenUserService tokenUserService;

    @PostMapping("/refresh")
    public CommonResponse<?> refreshToken(
            @RequestBody TokenReq tokenReq,
            @RequestHeader(JwtProviderService.REFRESH_TOKEN) String refreshToken,
        @Parameter(hidden = true) HttpServletResponse response
    ) {
        return tokenUserService.refreshToken(refreshToken, tokenReq, response);
    }
}
