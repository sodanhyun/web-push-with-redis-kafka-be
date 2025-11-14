/**
 * @file JwtAuthenticationFilter.java
 * @description HTTP 요청 헤더에서 JWT 토큰을 추출하고 유효성을 검증하여 Spring Security 컨텍스트에
 *              인증 정보를 설정하는 필터입니다. 특정 경로에 대해서는 필터링을 건너뛰도록 설정됩니다.
 */

package com.mytoyappbe.auth.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @class JwtAuthenticationFilter
 * @description 모든 HTTP 요청에 대해 한 번만 실행되는 필터로, JWT 토큰을 검증하고
 *              인증된 사용자의 정보를 Spring Security 컨텍스트에 설정합니다.
 *              `OncePerRequestFilter`를 상속받아 요청당 한 번의 필터 실행을 보장합니다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰의 생성, 검증, 정보 추출을 담당하는 제공자
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); // 요청 경로를 패턴 매칭하는 유틸리티

    /**
     * @property EXCLUDE_URLS
     * @description JWT 인증 필터링을 건너뛸 URL 패턴 목록입니다.
     *              로그인, 토큰 재발급, 웹소켓 핸드셰이크, 정적 리소스 등 인증이 필요 없는 경로를 포함합니다.
     */
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/ws/**",
            "/",
            "/index.html",
            "/login",
            "/assets/**",
            "/sw.js",
            "/manifest.webmanifest"
    );

    /**
     * @method doFilterInternal
     * @description 실제 필터링 로직을 수행하는 메서드입니다.
     *              요청 헤더에서 JWT 토큰을 추출하고 유효성을 검사하여 인증 정보를 설정합니다.
     * @param {HttpServletRequest} request - 현재 HTTP 요청 객체
     * @param {HttpServletResponse} response - 현재 HTTP 응답 객체
     * @param {FilterChain} filterChain - 다음 필터 또는 서블릿으로 요청을 전달하는 객체
     * @throws {ServletException} 서블릿 관련 예외 발생 시
     * @throws {IOException} 입출력 관련 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Request Header에서 JWT 토큰을 추출합니다.
        String token = resolveToken(request);

        // 추출된 토큰이 유효한지 검사합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰이 유효할 경우, 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장합니다.
            // SecurityContextHolder는 현재 스레드의 보안 컨텍스트를 저장하는 역할을 합니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response); // 다음 필터로 요청을 전달합니다.
    }

    /**
     * @method resolveToken
     * @description HTTP 요청 헤더에서 "Authorization" 필드로부터 JWT 토큰을 추출합니다.
     *              "Bearer " 접두사를 제거한 순수 토큰 문자열을 반환합니다.
     * @param {HttpServletRequest} request - 토큰을 추출할 HTTP 요청 객체
     * @returns {String} 추출된 JWT 토큰 문자열 또는 토큰이 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7); // "Bearer " (7자) 이후의 문자열이 토큰입니다.
        }
        return null;
    }

    /**
     * @method shouldNotFilter
     * @description 현재 요청이 JWT 인증 필터링을 건너뛸지 여부를 결정합니다.
     *              `EXCLUDE_URLS`에 정의된 경로 패턴과 일치하는 요청은 필터링을 수행하지 않습니다.
     * @param {HttpServletRequest} request - 현재 HTTP 요청 객체
     * @returns {boolean} 필터링을 건너뛰어야 하면 true, 그렇지 않으면 false
     * @throws {ServletException} 서블릿 관련 예외 발생 시
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestUri = request.getRequestURI();
        // EXCLUDE_URLS 목록의 패턴 중 현재 요청 URI와 일치하는 것이 있는지 확인합니다.
        return EXCLUDE_URLS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }
}