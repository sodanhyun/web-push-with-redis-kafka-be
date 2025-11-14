package com.mytoyappbe.websocket.handler;

import com.mytoyappbe.websocket.manager.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionHandler extends TextWebSocketHandler {

    private static final String USER_SESSIONS_KEY = "ws:users";

    private final WebSocketSessionManager sessionManager;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // userId를 경로에서 추출하는 로직 제거
        // 이 핸들러는 일반 WebSocket 핸들러이며, STOMP WebSocket과는 별개로 동작합니다.
        // STOMP WebSocket에서는 JWT 인증을 통해 SecurityContext에서 userId를 가져옵니다.
        // 따라서 이 핸들러에서는 userId를 직접 사용하지 않도록 수정합니다.
        log.info("WebSocket connection established. Session ID: {}", session.getId());
        sessionManager.addLocalSession(session.getId(), session); // 세션 ID를 키로 사용
        redisTemplate.opsForSet().add(USER_SESSIONS_KEY, session.getId()); // 세션 ID를 Redis에 저장
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // userId를 경로에서 추출하는 로직 제거
        log.info("WebSocket connection closed. Session ID: {}, Status: {}", session.getId(), status);
        sessionManager.removeLocalSession(session.getId()); // 세션 ID를 키로 사용
        redisTemplate.opsForSet().remove(USER_SESSIONS_KEY, session.getId()); // 세션 ID를 Redis에서 제거
    }
}
