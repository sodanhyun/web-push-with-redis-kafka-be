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
        String userId = getUserId(session);
        if (userId != null) {
            sessionManager.addLocalSession(userId, session);
            redisTemplate.opsForSet().add(USER_SESSIONS_KEY, userId);
            log.info("WebSocket connection established for user: {}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        if (userId != null) {
            sessionManager.removeLocalSession(userId);
            redisTemplate.opsForSet().remove(USER_SESSIONS_KEY, userId);
            log.info("WebSocket connection closed for user: {}. Status: {}", userId, status);
        }
    }

    private String getUserId(WebSocketSession session) {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String[] segments = path.split("/");
        if (segments.length > 0) {
            return segments[segments.length - 1];
        }
        return null;
    }
}
