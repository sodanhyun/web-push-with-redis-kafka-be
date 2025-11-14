/**
 * @file WebSocketAuthInterceptor.java
 * @description STOMP WebSocket 연결 시 JWT 토큰을 검증하고 Spring Security 컨텍스트에
 *              인증 정보를 설정하는 채널 인터셉터입니다.
 *              클라이언트의 CONNECT 메시지에서 JWT 토큰을 추출하여 유효성을 검사합니다.
 */

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

/**
 * @class WebSocketAuthInterceptor
 * @description STOMP WebSocket 메시지 채널을 가로채어 JWT 기반 인증을 처리하는 인터셉터입니다.
 *              클라이언트가 WebSocket 연결을 시도할 때(CONNECT 프레임) Authorization 헤더의 JWT 토큰을 검증하고,
 *              유효한 경우 사용자 인증 정보를 SecurityContext에 설정합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰의 생성, 검증, 정보 추출을 담당하는 제공자

    /**
     * @method preSend
     * @description 메시지가 채널로 전송되기 전에 호출됩니다.
     *              STOMP CONNECT 메시지의 경우 JWT 토큰을 검증하여 사용자 인증을 처리합니다.
     * @param {Message<?>} message - 전송될 메시지
     * @param {MessageChannel} channel - 메시지가 전송될 채널
     * @returns {Message<?>} 처리된 메시지 또는 null (메시지 전송 중단 시)
     * @throws {RuntimeException} 유효하지 않은 JWT 토큰이거나 Authorization 헤더가 없는 경우
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메시지 헤더에서 STOMP 관련 정보를 추출합니다.
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP CONNECT 명령인 경우 JWT 토큰을 검증합니다.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("WebSocket CONNECT: Authorization Header - {}", authorizationHeader);

            // Authorization 헤더가 존재하고 "Bearer "로 시작하는지 확인합니다.
            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7); // "Bearer " 접두사 제거
                // JWT 토큰의 유효성을 검증합니다.
                if (jwtTokenProvider.validateToken(jwt)) {
                    // 토큰이 유효하면 Authentication 객체를 생성하고 SecurityContext에 설정합니다.
                    Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                    accessor.setUser(authentication); // WebSocket 세션에 인증된 사용자 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication); // Spring SecurityContext에 인증 정보 저장
                    log.debug("WebSocket CONNECT: User authenticated - {}", authentication.getName());
                } else {
                    log.warn("WebSocket CONNECT: Invalid JWT token.");
                    // 유효하지 않은 토큰인 경우 연결을 거부하기 위해 RuntimeException을 발생시킵니다.
                    throw new RuntimeException("Invalid JWT token");
                }
            } else {
                log.warn("WebSocket CONNECT: Missing or invalid Authorization header.");
                // Authorization 헤더가 없거나 형식이 잘못된 경우 연결을 거부합니다.
                throw new RuntimeException("Missing or invalid Authorization header");
            }
        }
        // CONNECT 메시지가 아니지만 이미 인증된 사용자라면 SecurityContext에 다시 설정 (필요에 따라)
        else if (accessor.getUser() != null) {
            SecurityContextHolder.getContext().setAuthentication((Authentication) accessor.getUser());
        }
        // CONNECT 메시지가 아니고 인증되지 않은 사용자라면 SecurityContext 초기화
        else {
            SecurityContextHolder.clearContext();
        }

        return message; // 메시지를 다음 체인으로 전달합니다.
    }

    /**
     * @method postSend
     * @description 메시지가 채널로 전송된 후에 호출됩니다.
     *              STOMP DISCONNECT 메시지의 경우 SecurityContext를 정리합니다.
     * @param {Message<?>} message - 전송된 메시지
     * @param {MessageChannel} channel - 메시지가 전송된 채널
     * @param {boolean} sent - 메시지가 성공적으로 전송되었는지 여부
     */
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        // STOMP DISCONNECT 명령인 경우 SecurityContext를 정리합니다.
        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            SecurityContextHolder.clearContext();
            log.debug("WebSocket DISCONNECT: SecurityContext cleared.");
        }
    }
}