package com.mytoyappbe.auth.config.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // CONNECT 메시지일 경우 JWT 토큰 검증
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("WebSocket CONNECT: Authorization Header - {}", authorizationHeader);

            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7);
                if (jwtTokenProvider.validateToken(jwt)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    accessor.setUser(authentication); // 인증된 사용자 정보를 세션에 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContext에도 저장 (선택 사항)
                    log.debug("WebSocket CONNECT: User authenticated - {}", authentication.getName());
                } else {
                    log.warn("WebSocket CONNECT: Invalid JWT token.");
                    // 토큰 유효성 검증 실패 시 연결 거부 (또는 예외 발생)
                    // accessor.setLeaveMutable(true);
                    // accessor.setNativeHeader("error", "Invalid JWT token");
                    // return null; // 메시지 전송 중단
                    throw new RuntimeException("Invalid JWT token"); // 연결 거부
                }
            } else {
                log.warn("WebSocket CONNECT: Missing or invalid Authorization header.");
                // Authorization 헤더 없음
                // throw new RuntimeException("Missing or invalid Authorization header"); // 연결 거부
            }
        } else if (accessor.getUser() != null) {
            // CONNECT 메시지가 아니지만 이미 인증된 사용자라면 SecurityContext에 다시 설정 (필요에 따라)
            SecurityContextHolder.getContext().setAuthentication((Authentication) accessor.getUser());
        } else {
            // CONNECT 메시지가 아니고 인증되지 않은 사용자라면 SecurityContext 초기화
            SecurityContextHolder.clearContext();
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // 메시지 전송 후 SecurityContext 정리 (필요에 따라)
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            SecurityContextHolder.clearContext();
            log.debug("WebSocket DISCONNECT: SecurityContext cleared.");
        }
    }
}
