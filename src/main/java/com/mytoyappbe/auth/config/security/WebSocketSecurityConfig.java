/**
 * @file WebSocketSecurityConfig.java
 * @description STOMP over WebSocket 통신을 위한 Spring Security 설정을 담당하는 클래스입니다.
 *              클라이언트 인바운드 채널에 `WebSocketAuthInterceptor`를 등록하여
 *              웹소켓 연결 시 JWT 토큰을 검증하도록 합니다.
 */

package com.mytoyappbe.auth.config.security;

import com.mytoyappbe.auth.config.jwt.JwtTokenProvider;
import com.mytoyappbe.auth.config.jwt.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @class WebSocketSecurityConfig
 * @description STOMP over WebSocket 통신을 위한 Spring Security 설정을 정의하는 클래스입니다.
 *              `@EnableWebSocketMessageBroker`를 통해 STOMP 브로커 기능을 활성화하고,
 *              클라이언트 인바운드 채널에 JWT 인증 인터셉터를 등록하여 웹소켓 메시지 보안을 강화합니다.
 */
@Configuration // Spring 설정 클래스임을 나타냅니다.
@EnableWebSocketMessageBroker // STOMP 기반 웹소켓 메시지 브로커 기능을 활성화합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰의 생성, 검증, 정보 추출을 담당하는 제공자

    /**
     * @method configureClientInboundChannel
     * @description 클라이언트로부터 들어오는 메시지를 처리하는 인바운드 채널을 구성합니다.
     *              웹소켓 연결 시 JWT 토큰을 검증하는 `WebSocketAuthInterceptor`를 등록합니다.
     * @param {ChannelRegistration} registration - 채널 등록을 위한 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 클라이언트가 웹소켓 연결을 시도할 때 JWT 토큰을 검증하는 인터셉터를 등록합니다.
        // 이 인터셉터는 STOMP CONNECT 프레임의 헤더에서 JWT 토큰을 추출하여 인증을 수행합니다.
        registration.interceptors(new WebSocketAuthInterceptor(jwtTokenProvider));
    }
}