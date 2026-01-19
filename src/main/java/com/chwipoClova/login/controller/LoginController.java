package com.chwipoClova.login.controller;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.login.dto.AppleLoginDto;
import com.chwipoClova.login.dto.GoogleLoginDto;
import com.chwipoClova.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Login", description = "로그인 API")
@RequestMapping("login")
public class LoginController {
    private final LoginService loginService;

    @Operation(summary = "구글 로그인", description = "구글 로그인")
    @PostMapping("/googleLogin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))
        }
    )
    public CommonResponse googleLogin(@RequestBody GoogleLoginDto googleLoginDto, HttpServletResponse response) {
        return loginService.googleLogin(googleLoginDto, response);
    }

    @Operation(summary = "애플 로그인", description = "애플 로그인")
    @PostMapping("/appleLogin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = String.class)))
    }
    )
    public CommonResponse appleLogin(@RequestBody AppleLoginDto appleLoginDto, HttpServletResponse response) {
        return loginService.appleLogin(appleLoginDto, response);
    }
}
