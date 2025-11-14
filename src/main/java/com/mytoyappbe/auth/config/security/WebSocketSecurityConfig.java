package com.mytoyappbe.auth.config.security;

import com.mytoyappbe.auth.config.jwt.JwtTokenProvider;
import com.mytoyappbe.auth.config.jwt.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry; // MessageBrokerRegistry 임포트
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry; // StompEndpointRegistry 임포트
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 웹소켓 메시지 브로커 활성화
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    // 메시지 브로커 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // /topic, /queue로 시작하는 메시지를 브로커가 처리
        config.setApplicationDestinationPrefixes("/app"); // /app으로 시작하는 메시지를 @MessageMapping 핸들러가 처리
    }

    // STOMP 엔드포인트 등록
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 웹소켓 핸드셰이크 엔드포인트
                .setAllowedOriginPatterns("*") // 모든 Origin 허용 (CORS)
                .withSockJS(); // SockJS 사용
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 웹소켓 연결 시 JWT 토큰을 검증하는 인터셉터 등록
        registration.interceptors(new WebSocketAuthInterceptor(jwtTokenProvider));
    }
}
