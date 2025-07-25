package com.chwipoClova.common.utils;

import com.chwipoClova.common.service.UserDetailsServiceImpl;
import com.chwipoClova.token.dto.TokenDto;
import com.chwipoClova.token.entity.Token;
import com.chwipoClova.token.service.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenService tokenService;

    private static final long ACCESS_TIME = 2 * 60 * 60 * 1000L;

    private static final long REFRESH_TIME = 14 * 24 * 60 * 60 * 1000L;

    private static final int ACCESS_COOKIE_TIME = 2 * 60 * 60;

    public static final int REFRESH_COOKIE_TIME = 14 * 24 * 60 * 60;

    public static final String ACCESS_TOKEN = "accessToken";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String AUTHORIZATION = "Authorization";

    public static final String ACCESS = "Access";

    public static final String REFRESH = "Refresh";

    public static final String BEARER = "Bearer ";

    @Value("${cors.domain}")
    private String domain;


    @Value("${jwt.secretKey}")
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // bean으로 등록 되면서 딱 한번 실행이 됩니다.
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // header 토큰을 가져오는 기능
    public String getHeaderToken(HttpServletRequest request, String type) {
       /*String authorization = request.getHeader(AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER)) {
            return authorization.substring(7);
        } else {
            return null;
        }*/
        return request.getHeader(type);
    }

    // 토큰 생성
    public TokenDto createAllToken(String userId) {
        return new TokenDto(createToken(userId, ACCESS), createToken(userId, REFRESH), userId);
    }

    public String createToken(String id, String type) {

        Date date = new Date();

        long time = type.equals(ACCESS) ? ACCESS_TIME : REFRESH_TIME;

        if (StringUtils.equals(id, "224")) {
            time = 2 * 60 * 1000L;
        } else if (StringUtils.equals(id, "38")) {
            time = 30 * 60 * 1000L;
        }

        String loginId = type.equals(ACCESS) ? id : "";

        return Jwts.builder()
                .setSubject(loginId)
                .setExpiration(new Date(date.getTime() + time))
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();

    }

    // 토큰 검증
    public Boolean tokenValidation(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    // refreshToken 토큰 검증
    // db에 저장되어 있는 token과 비교
    // db에 저장한다는 것이 jwt token을 사용한다는 강점을 상쇄시킨다.
    // db 보다는 redis를 사용하는 것이 더욱 좋다. (in-memory db기 때문에 조회속도가 빠르고 주기적으로 삭제하는 기능이 기본적으로 존재합니다.)
    public Token selectRefreshToken(String token) {
        // 1차 토큰 검증
        if(Boolean.FALSE.equals(tokenValidation(token))) return null;
        //String idFromToken = getIdFromToken(token);
        //Long userId = Long.parseLong(idFromToken);
        // DB에 저장한 토큰 비교

        //Token refreshToken = tokenService.findById(token);

        // return refreshToken != null && token.equals(refreshToken.getRefreshToken());
        return tokenService.findById(token);
    }

    // 인증 객체 생성
    public Authentication createAuthentication(Long id) {
        UserDetails userDetails = userDetailsService.loadUserByUserId(id);
        // spring security 내에서 가지고 있는 객체입니다. (UsernamePasswordAuthenticationToken)
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 id 가져오는 기능
    public String getIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 어세스 토큰 헤더 설정
    public void setHeaderToken(HttpServletResponse response, String token, String type) {
        response.setHeader(type, token);
    }

    public void setCookieToken(HttpServletResponse response, String token, String type, String userId) {
        String cookieName = getCookieName(type);
        int cookieTime = getCookieTime(type);

        if (StringUtils.equals(userId, "224")) {
            cookieTime = 2 * 60;
        } else if (StringUtils.equals(userId, "38")) {
            cookieTime = 30 * 60;
        }

        ResponseCookie responseCookie = ResponseCookie.from(cookieName, token)
                .maxAge(cookieTime)
                .path("/")
                .secure(true)
                .domain(domain)
                .sameSite("None")
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void setDelCookieToken(HttpServletResponse response, String type) {
        String cookieName = getCookieName(type);

        ResponseCookie responseCookie = ResponseCookie.from(cookieName, null)
                .maxAge(0)
                .path("/")
                .secure(true)
                .domain(domain)
                .sameSite("None")
                .httpOnly(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public String getCookieToken(HttpServletRequest request, String type) {
        Cookie[] cookies = request.getCookies();
        String cookieName = getCookieName(type);

        AtomicReference<String> cookieToken = new AtomicReference<>();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(cookieName))
                    .findFirst()
                    .ifPresent(cookie -> {
                        cookieToken.set(cookie.getValue());
                    });
        }
        return cookieToken.get();
    }

    public void setDeleteHeaderToken(HttpServletResponse response, String type) {
        setHeaderToken(response, "", type);
    }

    public void deleteAllToken(HttpServletRequest request, HttpServletResponse response) {
        //setDeleteHeaderAccessToken(response);
        setDelCookieToken(response, JwtUtil.ACCESS);
        setDelCookieToken(response, JwtUtil.REFRESH);
        
        // redis refreshToken 삭제
        String refreshToken = getCookieToken(request, JwtUtil.REFRESH);
        tokenService.deleteById(refreshToken);
    }

    public void deleteAllTokenByHeader(HttpServletRequest request, HttpServletResponse response) {
        setDeleteHeaderToken(response, ACCESS_TOKEN);
        setDeleteHeaderToken(response, REFRESH_TOKEN);

        // redis refreshToken 삭제
        String refreshToken = getHeaderToken(request, REFRESH_TOKEN);
        tokenService.deleteById(refreshToken);
    }

    public void setResonseJwtToken(HttpServletResponse response, TokenDto tokenDto) {
        String userId = tokenDto.getUserId();
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        //setHeaderAccessToken(response, accessToken);
        setCookieToken(response, accessToken, JwtUtil.ACCESS, userId);
        setCookieToken(response, refreshToken, JwtUtil.REFRESH, userId);

        // redis refreshToken 저장
        Token newToken = new Token(refreshToken,  userId);
        tokenService.save(newToken);
    }

    public void setResonseJwtTokenByHeader(HttpServletResponse response, TokenDto tokenDto) {
        String userId = tokenDto.getUserId();
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        setHeaderToken(response, accessToken, ACCESS_TOKEN);
        setHeaderToken(response, accessToken, REFRESH_TOKEN);

        // redis refreshToken 저장
        Token newToken = new Token(refreshToken,  userId);
        tokenService.save(newToken);
    }

    public String getCookieName(String type) {
        return type.equals(ACCESS) ? ACCESS_TOKEN : REFRESH_TOKEN;
    }

    public int getCookieTime(String type) {
        return type.equals(ACCESS) ? ACCESS_COOKIE_TIME : REFRESH_COOKIE_TIME;
    }

}
