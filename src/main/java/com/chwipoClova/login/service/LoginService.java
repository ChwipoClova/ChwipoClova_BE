package com.chwipoClova.login.service;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.login.dto.AppleLoginDto;
import com.chwipoClova.login.dto.GoogleLoginDto;
import com.chwipoClova.oauth2.dto.GoogleOAuth2UserInfo;
import com.chwipoClova.oauth2.dto.UserInfo;
import com.chwipoClova.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class LoginService {
    private final UserService userService;

    public CommonResponse googleLogin(GoogleLoginDto googleLoginDto, HttpServletResponse response) {
        // UserInfo oauthUserInfo, HttpServletResponse response
        UserInfo oauthUserInfo = new GoogleOAuth2UserInfo(googleLoginDto);
        return userService.loginGoogle(oauthUserInfo, response);
    }

    public CommonResponse appleLogin(AppleLoginDto appleLoginDto, HttpServletResponse response) {
        // UserInfo oauthUserInfo, HttpServletResponse response
        //UserInfo oauthUserInfo = new GoogleOAuth2UserInfo(googleLoginDto);
        return userService.appleLogin(appleLoginDto, response);
    }
}
