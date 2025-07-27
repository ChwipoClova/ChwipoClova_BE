package com.chwipoClova.common.service;

import com.chwipoClova.token.dto.TokenDto;
import com.chwipoClova.token.entity.Token;
import com.chwipoClova.token.service.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
@Primary
public class JwtHeaderServiceImpl implements JwtProviderService {

    private final UserDetailsServiceImpl userDetailsService;
    private final TokenService tokenService;

    @Value("${jwt.secretKey}")
    private String secretKey;

    private Key key;
    private SignatureAlgorithm signatureAlgorithm;

    // bean으로 등록 되면서 딱 한번 실행이 됩니다.
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
        signatureAlgorithm = SignatureAlgorithm.HS256;
    }

    // header 토큰을 가져오는 기능
    public String getToken(HttpServletRequest request, String type) {
        String token = request.getHeader(type);

        if (token != null && token.startsWith(JwtProviderService.BEARER)) {
            return token.substring(JwtProviderService.BEARER.length()); // "Bearer "를 제외한 토큰 부분 추출
        }
        return null;
    }

    // 토큰 생성
    public TokenDto createAllToken(String userId) {
        return new TokenDto(createToken(userId, ACCESS_TOKEN), createToken(userId, REFRESH_TOKEN), userId);
    }

    public String createToken(String id, String type) {
        Date date = new Date();
        long time = type.equals(ACCESS_TOKEN) ? ACCESS_TIME : REFRESH_TIME;
        String loginId = type.equals(ACCESS_TOKEN) ? id : "";

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
        if(Boolean.FALSE.equals(tokenValidation(token))) return null;
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
    public void setToken(HttpServletResponse response, String token, String type) {
        response.setHeader(type, token);
    }
    public void setDelToken(HttpServletResponse response, String type) {
        setToken(response, "", type);
    }

    public void deleteAllToken(HttpServletRequest request, HttpServletResponse response) {
        setDelToken(response, ACCESS_TOKEN);
        setDelToken(response, REFRESH_TOKEN);

        // redis refreshToken 삭제
        String refreshToken = getToken(request, REFRESH_TOKEN);
        tokenService.deleteById(refreshToken);
    }

    public void setResponseNmtoken(HttpServletResponse response, TokenDto tokenDto) {
        String userId = tokenDto.getUserId();
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        setToken(response, accessToken, ACCESS_TOKEN);
        setToken(response, refreshToken, REFRESH_TOKEN);

        // redis refreshToken 저장
        Token newToken = new Token(refreshToken,  userId);
        tokenService.save(newToken);
    }
}