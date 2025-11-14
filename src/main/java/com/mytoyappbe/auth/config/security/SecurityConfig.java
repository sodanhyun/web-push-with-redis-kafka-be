/**
 * @file SecurityConfig.java
 * @description Spring Security 설정을 담당하는 클래스입니다.
 *              JWT 기반 인증, CSRF 비활성화, 세션 관리, 인가 규칙 정의, 예외 처리 등을 구성합니다.
 */

package com.mytoyappbe.auth.config.security;

import com.mytoyappbe.auth.config.jwt.JwtAuthenticationFilter;
import com.mytoyappbe.auth.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @class SecurityConfig
 * @description Spring Security 설정을 정의하는 클래스입니다.
 *              JWT 기반 인증 및 인가 규칙을 구성하고, CSRF, 세션 관리 등을 설정합니다.
 */
@Configuration // Spring 설정 클래스임을 나타냅니다.
@EnableWebSecurity // Spring Security를 활성화합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증을 담당하는 제공자
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // JWT 인증 실패 시 401 응답을 처리하는 진입점

    /**
     * @method filterChain
     * @description HTTP 보안 필터 체인을 구성하는 메서드입니다.
     * @param {HttpSecurity} http - HttpSecurity 객체를 통해 보안 설정을 구성합니다.
     * @returns {SecurityFilterChain} 구성된 보안 필터 체인
     * @throws {Exception} 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF (Cross-Site Request Forgery) 보호를 비활성화합니다.
                // JWT를 사용하는 REST API에서는 세션을 사용하지 않으므로 CSRF 보호가 필요 없습니다.
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 관리 정책을 STATELESS로 설정합니다.
                // JWT를 사용하므로 서버에 세션을 저장하지 않습니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTP Basic 인증을 비활성화합니다.
                .httpBasic(AbstractHttpConfigurer::disable)
                // 폼 로그인 기능을 비활성화합니다.
                .formLogin(AbstractHttpConfigurer::disable)
                // 예외 처리 설정을 구성합니다.
                // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 JwtAuthenticationEntryPoint를 호출하여 401 Unauthorized 응답을 반환합니다.
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // HTTP 요청에 대한 인가 규칙을 설정합니다.
                .authorizeHttpRequests(authorize -> authorize
                        // 로그인 API 엔드포인트는 모든 사용자에게 허용합니다.
                        .requestMatchers("/api/auth/login").permitAll()
                        // Refresh Token 재발급 API 엔드포인트는 모든 사용자에게 허용합니다.
                        .requestMatchers("/api/auth/refresh").permitAll()
                        // 웹소켓 핸드셰이크 엔드포인트는 모든 사용자에게 허용합니다.
                        .requestMatchers("/ws/**").permitAll()
                        // 프론트엔드 로그인 페이지 및 정적 리소스(루트, index.html, login, assets, 서비스 워커, 매니페스트)는 모든 사용자에게 허용합니다.
                        .requestMatchers("/", "/index.html", "/login", "/assets/**", "/sw.js", "/manifest.webmanifest").permitAll()
                        // 위에서 명시적으로 허용한 경로를 제외한 모든 요청은 인증된 사용자만 접근할 수 있도록 합니다.
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가합니다.
                // 이 필터는 요청 헤더에서 JWT 토큰을 추출하고 유효성을 검증합니다.
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * @method passwordEncoder
     * @description 비밀번호 암호화를 위한 `BCryptPasswordEncoder` 빈을 등록합니다.
     * @returns {PasswordEncoder} BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}