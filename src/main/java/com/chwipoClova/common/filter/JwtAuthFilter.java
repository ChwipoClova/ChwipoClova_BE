package com.chwipoClova.common.filter;

import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.service.LogService;
import com.chwipoClova.common.utils.JwtUtil;
import com.chwipoClova.token.entity.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    //private String TOKEN_PREFIX = "Bearer ";

    private final String[] authorizeUrl;

    private final LogService logService;

    @Override
    // HTTP 요청이 오면 WAS(tomcat)가 HttpServletRequest, HttpServletResponse 객체를 만들어 줍니다.
    // 만든 인자 값을 받아옵니다.
    // 요청이 들어오면 diFilterInternal 이 딱 한번 실행된다.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // WebSecurityConfig 에서 보았던 UsernamePasswordAuthenticationFilter 보다 먼저 동작을 하게 됩니다.
        // 로그인 로그아웃은 제외
        String url = request.getRequestURI();

        if (!Arrays.stream(authorizeUrl).anyMatch(url::equals)) {
            // Access / Refresh 헤더와 쿠키에서 토큰을 가져옴.
            String accessToken = jwtUtil.getCookieToken(request, JwtUtil.ACCESS);
            String refreshToken = jwtUtil.getCookieToken(request, JwtUtil.REFRESH);

            String loginId;
            if(accessToken != null && jwtUtil.tokenValidation(accessToken)) {
                loginId = jwtUtil.getIdFromToken(accessToken);
            } else if (refreshToken != null) { // 어세스 토큰이 만료된 상황 && 리프레시 토큰 또한 존재하는 상황
                // 리프레시 토큰 검증 && 리프레시 토큰 DB에서  토큰 존재유무 확인
                Token isRefreshToken = jwtUtil.selectRefreshToken(refreshToken);
                // 리프레시 토큰이 유효하고 리프레시 토큰이 DB와 비교했을때 똑같다면
                if (isRefreshToken != null) {
                    // 리프레시 토큰으로 아이디 정보 가져오기
                    loginId = isRefreshToken.getUserId();
                    log.info("유저 갱신 {}", loginId);

                    logService.refreshUserLogSave(Long.parseLong(loginId), "유저 갱신 " + loginId);
                } else { // 리프레시 토큰이 만료 || 리프레시 토큰이 DB와 비교했을때 똑같지 않다면
                    jwtExceptionHandler(response, "RefreshToken Expired", HttpStatus.BAD_REQUEST);
                    return;
                }
            } else { // 리프레시 토큰이 만료 || 리프레시 토큰이 DB와 비교했을때 똑같지 않다면
                jwtExceptionHandler(response, "RefreshToken Expired", HttpStatus.BAD_REQUEST);
                return;
            }
            // 새로운 어세스 토큰 발급
            String newAccessToken = jwtUtil.createToken(loginId, JwtUtil.ACCESS);
            jwtUtil.setCookieToken(response, newAccessToken, JwtUtil.ACCESS, loginId);
            setAuthentication(loginId);
        }
        filterChain.doFilter(request,response);
    }

    // SecurityContext 에 Authentication 객체를 저장합니다.
    public void setAuthentication(String subject) {
        try {
            if (StringUtils.isNotBlank(subject)) {
                Long id = Long.parseLong(subject);
                Authentication authentication = jwtUtil.createAuthentication(id);
                // security가 만들어주는 securityContextHolder 그 안에 authentication을 넣어줍니다.
                // security가 securitycontextholder에서 인증 객체를 확인하는데
                // jwtAuthfilter에서 authentication을 넣어주면 UsernamePasswordAuthenticationFilter 내부에서 인증이 된 것을 확인하고 추가적인 작업을 진행하지 않습니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("token id 변환에 실패했습니다. {}", e);
        }
    }

    // Jwt 예외처리
    public void jwtExceptionHandler(HttpServletResponse response, String msg, HttpStatus status) {
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
