/**
 * @file AuthController.java
 * @description 사용자 인증(로그인) 및 JWT 토큰(Access Token, Refresh Token) 재발급 요청을 처리하는 REST 컨트롤러입니다.
 *              클라이언트로부터 로그인 정보나 Refresh Token을 받아 `AuthService`를 통해 비즈니스 로직을 수행합니다.
 */

package com.mytoyappbe.auth.controller;

import com.mytoyappbe.auth.config.jwt.TokenInfo;
import com.mytoyappbe.auth.dto.LoginRequestDto;
import com.mytoyappbe.auth.dto.RefreshTokenRequestDto;
import com.mytoyappbe.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class AuthController
 * @description 사용자 인증 및 토큰 재발급 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 *              `/api/auth` 경로로 들어오는 요청을 매핑합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/auth") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/auth" 경로를 기본으로 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다. (의존성 주입)
public class AuthController {

    private final AuthService authService; // 인증 관련 비즈니스 로직을 처리하는 서비스

    /**
     * @method login
     * @description 사용자 로그인 요청을 처리하고, 인증 성공 시 JWT 토큰 정보를 반환합니다.
     * @param {LoginRequestDto} loginRequestDto - 로그인 요청 본문 (사용자 이름, 비밀번호)
     * @returns {TokenInfo} 생성된 Access Token 및 Refresh Token 정보를 담은 객체
     */
    @PostMapping("/login") // HTTP POST 요청을 "/api/auth/login" 경로에 매핑합니다.
    public TokenInfo login(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("login request. username={}, password={}", loginRequestDto.getUsername(), loginRequestDto.getPassword());
        TokenInfo tokenInfo = authService.login(loginRequestDto); // AuthService를 통해 로그인 처리 및 토큰 생성
        return tokenInfo;
    }

    /**
     * @method refresh
     * @description Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 재발급합니다.
     * @param {RefreshTokenRequestDto} refreshTokenRequestDto - Refresh Token을 포함하는 요청 본문
     * @returns {TokenInfo} 새로 생성된 Access Token 및 Refresh Token 정보를 담은 객체
     */
    @PostMapping("/refresh") // HTTP POST 요청을 "/api/auth/refresh" 경로에 매핑합니다.
    public TokenInfo refresh(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        log.info("refresh token request.");
        TokenInfo tokenInfo = authService.refresh(refreshTokenRequestDto.getRefreshToken()); // AuthService를 통해 토큰 재발급 처리
        return tokenInfo;
    }
}