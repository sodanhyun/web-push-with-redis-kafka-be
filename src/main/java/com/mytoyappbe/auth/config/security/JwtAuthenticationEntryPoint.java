/**
 * @file JwtAuthenticationEntryPoint.java
 * @description JWT 인증 실패 시 401 Unauthorized 응답을 반환하는 커스텀 AuthenticationEntryPoint입니다.
 *              Spring Security 필터 체인에서 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출됩니다.
 */

package com.mytoyappbe.auth.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @class JwtAuthenticationEntryPoint
 * @description Spring Security 필터 체인에서 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출되는
 *              커스텀 `AuthenticationEntryPoint` 구현체입니다.
 *              JWT 토큰 인증 실패 시 HTTP 401 Unauthorized 응답을 클라이언트에 반환합니다.
 */
@Component // Spring 컨테이너에 빈으로 등록합니다.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * @method commence
     * @description 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출됩니다.
     *              클라이언트에게 401 Unauthorized 응답을 보냅니다.
     * @param {HttpServletRequest} request - 클라이언트의 HTTP 요청
     * @param {HttpServletResponse} response - 서버의 HTTP 응답
     * @param {AuthenticationException} authException - 발생한 인증 예외
     * @throws {IOException} 응답 작성 중 발생할 수 있는 입출력 예외
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 유효한 자격 증명이 없는 상태에서 보호된 리소스에 접근할 때
        // HTTP 상태 코드 401 (Unauthorized)와 함께 "Unauthorized" 메시지를 응답으로 보냅니다.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}