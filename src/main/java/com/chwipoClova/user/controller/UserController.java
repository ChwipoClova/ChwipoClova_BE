package com.chwipoClova.user.controller;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.user.request.UserLoginReq;
import com.chwipoClova.user.response.UserInfoRes;
import com.chwipoClova.user.response.UserLoginRes;
import com.chwipoClova.user.response.UserSnsUrlRes;
import com.chwipoClova.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 API")
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "카카오 로그인 URL", description = "카카오 로그인 URL")
    @GetMapping("/getKakaoUrl")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    }
    )
    public UserSnsUrlRes getKakaoUrl() throws Exception {
        return userService.getKakaoUrl();
    }

    @Operation(summary = "카카오 로그인", description = "카카오 로그인")
    @GetMapping("/kakaoCallback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UserLoginRes.class))),
            @ApiResponse(responseCode = "700", description = "신규 가입되었습니다.", content = @Content(schema = @Schema(implementation = String.class)))
    }
    )
    public CommonResponse kakaoCallback(@Schema(description = "로그인코드", example = "1", name = "code") @RequestParam(name = "code") String code, HttpServletResponse response) throws Exception {
        return userService.kakaoLogin(code, response);
    }


    @Operation(summary = "유저 정보 조회 (테스트용)", description = "유저 정보 조회 (테스트용)")
    @GetMapping("/getUserInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    }
    )
    public UserInfoRes getUserInfo(
            @Schema(description = "이메일", example = "test@naver.com", name = "email") @RequestParam(name = "email") String email
    ) {
        return userService.selectUserInfo(email);
    }
}
