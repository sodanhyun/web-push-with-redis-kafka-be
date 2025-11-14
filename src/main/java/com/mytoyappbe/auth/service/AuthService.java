/**
 * @file AuthService.java
 * @description 사용자 인증(로그인) 및 JWT 토큰(Access Token, Refresh Token) 재발급 로직을 처리하는 서비스 클래스입니다.
 *              Spring Security의 인증 메커니즘과 JWT 토큰 제공자를 활용하여 사용자 인증 흐름을 관리합니다.
 */

package com.mytoyappbe.auth.service;

import com.mytoyappbe.auth.config.jwt.JwtTokenProvider;
import com.mytoyappbe.auth.config.jwt.TokenInfo;
import com.mytoyappbe.auth.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @class AuthService
 * @description 사용자 인증(로그인) 및 JWT 토큰 재발급과 관련된 비즈니스 로직을 제공하는 서비스입니다.
 *              `AuthenticationManagerBuilder`를 통해 사용자 인증을 수행하고,
 *              `JwtTokenProvider`를 통해 JWT 토큰을 생성 및 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션으로 설정
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder; // 사용자 인증을 관리하는 빌더
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증을 담당하는 제공자

    /**
     * @method login
     * @description 사용자 로그인 요청을 처리하고, 인증 성공 시 JWT 토큰(Access Token, Refresh Token)을 발급합니다.
     * @param {LoginRequestDto} loginRequestDto - 로그인 요청 정보 (사용자 ID, 비밀번호)
     * @returns {TokenInfo} 생성된 JWT 토큰 정보를 담은 객체
     */
    @Transactional // 로그인 시 토큰 생성은 쓰기 작업이므로 트랜잭션 적용
    public TokenInfo login(LoginRequestDto loginRequestDto) {
        // Login ID/PW를 기반으로 Authentication 객체 (인증 전) 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        // 실제 사용자 비밀번호 검증이 이루어지는 부분
        // authenticate 메서드 호출 시 CustomUserDetailsService의 loadUserByUsername 메서드가 실행됩니다.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증된 정보를 기반으로 JWT 토큰 생성
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        return tokenInfo;
    }

    /**
     * @method refresh
     * @description Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 재발급합니다.
     * @param {String} refreshToken - 클라이언트로부터 받은 Refresh Token
     * @returns {TokenInfo} 새로 생성된 JWT 토큰 정보를 담은 객체
     * @throws {RuntimeException} Refresh Token이 유효하지 않은 경우
     */
    @Transactional // 토큰 재발급은 쓰기 작업이므로 트랜잭션 적용
    public TokenInfo refresh(String refreshToken) {
        // 1. Refresh Token의 유효성을 검사합니다.
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. Refresh Token에서 사용자 ID(Subject)를 추출합니다.
        String userId = jwtTokenProvider.getSubject(refreshToken);

        // 3. 추출된 사용자 ID를 기반으로 새로운 Authentication 객체를 생성합니다.
        //    (권한 정보는 임시로 'ROLE_USER'를 부여하지만, 실제 구현에서는 DB에서 사용자 권한을 조회하여 정확한 권한을 부여해야 합니다.)
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails principal = new User(userId, "", authorities); // 비밀번호는 필요 없으므로 빈 문자열
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "", authorities);

        // 4. 새로운 Access Token 및 Refresh Token 생성
        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);

        return newTokenInfo;
    }
}