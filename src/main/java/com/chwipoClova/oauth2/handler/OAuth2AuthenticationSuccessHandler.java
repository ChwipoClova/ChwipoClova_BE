package com.chwipoClova.oauth2.handler;

import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.common.service.JwtCookieServiceImpl;
import com.chwipoClova.oauth2.dto.OAuth2UserInfoRecord;
import com.chwipoClova.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${home.url}")
    private String homeUrl;

    @Value("${home.error}")
    private String error;

    private final JwtCookieServiceImpl jwtCookieService;

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2UserInfoRecord userInfoRecord = getOAuth2UserPrincipal(authentication);

        String url = "";
        if (userInfoRecord == null || userInfoRecord.userInfo() == null) {
            url = UriComponentsBuilder.fromUriString(homeUrl+error)
                    .build().toUriString();
        } else {
            CommonResponse commonResponse = userService.loginGoogle(userInfoRecord.userInfo(), response);
            String code = commonResponse.getCode();

            if (StringUtils.equals(code, MessageCode.NEW_USER.getCode())) {
                url = UriComponentsBuilder.fromUriString(homeUrl)
                        .queryParam("signup", "true")
                        .build().toUriString();
            } else {
                url = UriComponentsBuilder.fromUriString(homeUrl)
                        .build().toUriString();
            }
        }
        //clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private OAuth2UserInfoRecord getOAuth2UserPrincipal(Authentication authentication) {
        Object userInfo = authentication.getPrincipal();

        if (userInfo instanceof OAuth2UserInfoRecord) {
            return (OAuth2UserInfoRecord) userInfo;
        }
        return null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        jwtCookieService.deleteAllToken(request, response);
    }
}
