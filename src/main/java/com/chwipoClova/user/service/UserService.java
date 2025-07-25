package com.chwipoClova.user.service;

import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.common.service.JwtProviderService;
import com.chwipoClova.common.service.LogService;
import com.chwipoClova.oauth2.dto.UserInfo;
import com.chwipoClova.token.dto.TokenDto;
import com.chwipoClova.user.dto.KakaoToken;
import com.chwipoClova.user.dto.KakaoUserInfo;
import com.chwipoClova.user.entity.User;
import com.chwipoClova.user.repository.UserRepository;
import com.chwipoClova.user.request.UserLogoutReq;
import com.chwipoClova.user.response.UserInfoRes;
import com.chwipoClova.user.response.UserLoginRes;
import com.chwipoClova.user.response.UserSnsUrlRes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Optional;


@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final RestTemplate restTemplate;

    private final JwtProviderService jwtProviderService;

    @Value("${kakao.url.auth}")
    private String kakaoAuthUrl;

    @Value("${kakao.url.token}")
    private String tokenUrl;

    @Value("${kakao.url.api}")
    private String apiUrl;

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.client_secret}")
    private String clientSecret;

    @Value("${kakao.grant_type}")
    private String grantType;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    @Value("${kakao.redirect_local_uri}")
    private String redirectLocalUri;

    private final LogService logService;

    public UserSnsUrlRes getKakaoUrl() {
        String kakaoUrl = kakaoAuthUrl + "?response_type=code" + "&client_id=" + clientId
                + "&redirect_uri=" + redirectLocalUri;
        return UserSnsUrlRes.builder()
                .url(kakaoUrl)
                .build();
    }

    @Transactional
    public CommonResponse kakaoLogin(String code, HttpServletResponse response) {
        KakaoToken kakaoToken = requestAccessToken(code);
        KakaoUserInfo kakaoUserInfo = requestOauthInfo(kakaoToken);
        return login(kakaoUserInfo, response);
    }

    public CommonResponse login(UserInfo oauthUserInfo, HttpServletResponse response) {
        String snsId = oauthUserInfo.getId();
        String email = oauthUserInfo.getEmail();
        String nickname = oauthUserInfo.getNickname();
        Integer snsType = oauthUserInfo.getOAuthProvider().getCode();
        String thumbnailImageUrl = oauthUserInfo.getThumbnailImageUrl();
        String profileImageUrl = oauthUserInfo.getProfileImageUrl();

        Optional<User> userInfo = userRepository.findBySnsTypeAndSnsId(snsType, snsId);

        // 유저 정보가 있다면 업데이트 없으면 등록
        if (userInfo.isPresent()) {
            User userInfoRst = userInfo.get();

            Long userId = userInfoRst.getUserId();

            String strUserId = String.valueOf(userId);

            // 로그인 할때마다 토큰 새로 발급(갱신)
            TokenDto tokenDto = jwtProviderService.createAllToken(strUserId);

            // response 헤더에 Access Token / Refresh Token 넣음
            jwtProviderService.setResponseNmtoken(response, tokenDto);

            UserLoginRes userLoginRes = UserLoginRes.builder()
                    .snsId(userInfoRst.getSnsId())
                    .userId(userId)
                    .email(userInfoRst.getEmail())
                    .name(userInfoRst.getName())
                    .snsType(userInfoRst.getSnsType())
                    .thumbnailImage(userInfoRst.getThumbnailImage())
                    .profileImage(userInfoRst.getProfileImage())
                    .regDate(userInfoRst.getRegDate())
                    .modifyDate(userInfoRst.getModifyDate())
                    .build();
            log.info("기존유저 {}, {}",userLoginRes.getUserId(), userLoginRes.getName());
            // API 로그 적재
            logService.loginUserLogSave(userLoginRes.getUserId(), "기존유저 " + userLoginRes.getUserId() + "," + userLoginRes.getName());
            return new CommonResponse<>(String.valueOf(HttpStatus.OK.value()), userLoginRes, HttpStatus.OK.getReasonPhrase());
        } else {
            User user = User.builder()
                    .snsId(snsId)
                    .email(email)
                    .name(nickname)
                    .snsType(snsType)
                    .thumbnailImage(thumbnailImageUrl)
                    .profileImage(profileImageUrl)
                    .regDate(new Date())
                    .build();
            User userResult = userRepository.save(user);
            log.info("신규유저 {}, {}",userResult.getUserId(), userResult.getName());
            // API 로그 적재
            logService.newUserLogSave(userResult.getUserId(), "신규유저 " + userResult.getUserId() + userResult.getName());
            return new CommonResponse<>(MessageCode.NEW_USER.getCode(), null, MessageCode.NEW_USER.getMessage());
        }
    }

    public CommonResponse loginGoogle(UserInfo oauthUserInfo, HttpServletResponse response) {
        String snsId = oauthUserInfo.getId();
        String email = oauthUserInfo.getEmail();
        String nickname = oauthUserInfo.getNickname();
        Integer snsType = oauthUserInfo.getOAuthProvider().getCode();
        String thumbnailImageUrl = oauthUserInfo.getThumbnailImageUrl();
        String profileImageUrl = oauthUserInfo.getProfileImageUrl();

        Optional<User> userInfo = userRepository.findBySnsTypeAndSnsId(snsType, snsId);

        // 유저 정보가 있다면 업데이트 없으면 등록
        if (userInfo.isPresent()) {
            User userInfoRst = userInfo.get();

            Long userId = userInfoRst.getUserId();

            String strUserId = String.valueOf(userId);

            // 로그인 할때마다 토큰 새로 발급(갱신)
            TokenDto tokenDto = jwtProviderService.createAllToken(strUserId);

            // response 헤더에 Access Token / Refresh Token 넣음
            jwtProviderService.setResponseNmtoken(response, tokenDto);

            UserLoginRes userLoginRes = UserLoginRes.builder()
                    .snsId(userInfoRst.getSnsId())
                    .userId(userId)
                    .email(userInfoRst.getEmail())
                    .name(userInfoRst.getName())
                    .snsType(userInfoRst.getSnsType())
                    .thumbnailImage(userInfoRst.getThumbnailImage())
                    .profileImage(userInfoRst.getProfileImage())
                    .regDate(userInfoRst.getRegDate())
                    .modifyDate(userInfoRst.getModifyDate())
                    .build();
            log.info("기존유저 {}, {}",userLoginRes.getUserId(), userLoginRes.getName());
            // API 로그 적재
            logService.loginUserLogSave(userLoginRes.getUserId(), "기존유저 " + userLoginRes.getUserId() + "," + userLoginRes.getName());
            return new CommonResponse<>(String.valueOf(HttpStatus.OK.value()), userLoginRes, HttpStatus.OK.getReasonPhrase());
        } else {
            User user = User.builder()
                    .snsId(snsId)
                    .email(email)
                    .name(nickname)
                    .snsType(snsType)
                    .thumbnailImage(thumbnailImageUrl)
                    .profileImage(profileImageUrl)
                    .regDate(new Date())
                    .build();
            User userResult = userRepository.save(user);
            log.info("신규유저 {}, {}",userResult.getUserId(), userResult.getName());

            Long userId = userResult.getUserId();

            String strUserId = String.valueOf(userId);

            // 로그인 할때마다 토큰 새로 발급(갱신)
            TokenDto tokenDto = jwtProviderService.createAllToken(strUserId);

            // response 헤더에 Access Token / Refresh Token 넣음
            jwtProviderService.setResponseNmtoken(response, tokenDto);

            // API 로그 적재
            logService.newUserLogSave(userResult.getUserId(), "신규유저 " + userResult.getUserId() + userResult.getName());
            return new CommonResponse<>(MessageCode.NEW_USER.getCode(), null, MessageCode.NEW_USER.getMessage());
        }
    }

    public KakaoToken requestAccessToken(String code) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();;
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, httpHeaders);

        KakaoToken response = restTemplate.postForObject(tokenUrl, request, KakaoToken.class);

        assert response != null;
        return response;
    }

    public KakaoUserInfo requestOauthInfo(KakaoToken kakaoToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization", "Bearer " + kakaoToken.getAccessToken());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();;
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, httpHeaders);
        KakaoUserInfo response = restTemplate.postForObject(apiUrl, request, KakaoUserInfo.class);

        assert response != null;
        return response;
    }

    public CommonResponse logout(HttpServletRequest request, HttpServletResponse response, UserLogoutReq userLogoutReq) {
        // 로그아웃은 무조건 성공
        try {
            jwtProviderService.deleteAllToken(request, response);
        } catch (Exception e) {
            log.error("로그아웃 에러 발생 {}", e.getMessage());
        }
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    public UserInfoRes selectUserInfoForUserId(Long userId) {
        Optional<User> usersInfo = userRepository.findById(userId);
        if (usersInfo.isEmpty()) {
            throw new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode());
        }

        User user = usersInfo.get();

        return UserInfoRes.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .thumbnailImage(user.getThumbnailImage())
                .profileImage(user.getProfileImage())
                .regDate(user.getRegDate())
                .modifyDate(user.getModifyDate())
                .build();
    }
}
