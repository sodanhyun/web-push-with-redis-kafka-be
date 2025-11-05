package com.mytoyappbe.handler;

import com.mytoyappbe.manager.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

/**
 * 클라이언트의 WebSocket 연결을 처리하는 핸들러 클래스입니다.
 * <p>
 * {@link TextWebSocketHandler}를 상속받아 텍스트 기반의 WebSocket 메시지를 처리하며,
 * 연결 수립, 메시지 수신, 연결 종료 등의 이벤트를 관리합니다.
 * 이 핸들러는 로컬 WebSocket 세션을 관리하고, Redis를 통해 분산 환경에서 연결된 사용자 상태를 추적합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    /**
     * Redis에 연결된 사용자 ID를 저장하는 Set의 키입니다.
     * 이 Set은 현재 어떤 사용자들이 WebSocket에 연결되어 있는지 분산 환경에서 추적하는 데 사용됩니다.
     */
    private static final String USER_SESSIONS_KEY = "ws:users";

    /**
     * 현재 서버 인스턴스에 연결된 로컬 WebSocket 세션들을 관리하는 매니저입니다.
     * {@link WebSocketSessionManager}는 실제 {@link WebSocketSession} 객체를 메모리에 보관합니다.
     */
    private final WebSocketSessionManager sessionManager;

    /**
     * Redis 데이터베이스와 상호작용하기 위한 Spring의 {@link RedisTemplate}입니다.
     * 여기서는 주로 {@code opsForSet()}을 사용하여 Redis Set에 사용자 ID를 추가/제거하는 데 사용됩니다.
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 클라이언트와 WebSocket 연결이 성공적으로 수립된 후에 호출됩니다.
     * <p>
     * 1. 연결 URI에서 사용자 ID를 추출합니다.
     * 2. 추출된 사용자 ID와 {@link WebSocketSession}을 {@link WebSocketSessionManager}에 등록하여 로컬 세션을 관리합니다.
     * 3. Redis의 {@code ws:users} Set에 해당 사용자 ID를 추가하여 분산 환경에서 사용자의 연결 상태를 알립니다.
     *
     * @param session 새로 수립된 WebSocket 세션
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        if (userId != null) {
            sessionManager.addLocalSession(userId, session);
            redisTemplate.opsForSet().add(USER_SESSIONS_KEY, userId);
            log.info("WebSocket connection established for user: {}", userId);
        }
    }

    /**
     * 클라이언트와 WebSocket 연결이 종료된 후에 호출됩니다.
     * <p>
     * 1. 연결 URI에서 사용자 ID를 추출합니다.
     * 2. {@link WebSocketSessionManager}에서 해당 사용자 ID의 로컬 세션을 제거합니다.
     * 3. Redis의 {@code ws:users} Set에서 해당 사용자 ID를 제거하여 분산 환경에서 사용자의 연결 해제 상태를 알립니다.
     *
     * @param session 종료된 WebSocket 세션
     * @param status 연결 종료 상태 (예: 정상 종료, 에러로 인한 종료 등)
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        if (userId != null) {
            sessionManager.removeLocalSession(userId);
            redisTemplate.opsForSet().remove(USER_SESSIONS_KEY, userId);
            log.info("WebSocket connection closed for user: {}. Status: {}", userId, status);
        }
    }

    /**
     * WebSocket 세션의 URI에서 사용자 ID를 추출합니다.
     * <p>
     * 연결 URI는 일반적으로 {@code /ws/test/{userId}}와 같은 형식을 가집니다.
     * 이 메서드는 URI 경로의 마지막 세그먼트에서 사용자 ID를 파싱합니다.
     *
     * @param session 사용자 ID를 추출할 WebSocket 세션
     * @return 추출된 사용자 ID, 또는 추출할 수 없는 경우 null
     */
    private String getUserId(WebSocketSession session) {
        // WebSocketSession의 URI에서 경로를 가져옵니다.
        String path = Objects.requireNonNull(session.getUri()).getPath();
        // 경로를 '/' 기준으로 분할합니다.
        String[] segments = path.split("/");
        // 경로의 마지막 세그먼트가 userId라고 가정합니다.
        if (segments.length > 0) {
            return segments[segments.length - 1];
        }
        return null;
    }
}
