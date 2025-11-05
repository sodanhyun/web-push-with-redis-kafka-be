package com.mytoyappbe.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 현재 서버 인스턴스에 연결된 WebSocket 세션들을 관리하는 매니저 클래스입니다.
 * <p>
 * 분산 환경에서 각 서버는 자신에게 직접 연결된 클라이언트의 {@link WebSocketSession} 객체를
 * 이 매니저를 통해 메모리({@link ConcurrentHashMap})에 보관합니다.
 * 이 클래스는 세션 추가, 조회, 제거 및 특정 세션으로 메시지 전송 기능을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    /**
     * 사용자 ID를 키로, {@link WebSocketSession} 객체를 값으로 하는 맵입니다.
     * {@link ConcurrentHashMap}을 사용하여 다중 스레드 환경에서 안전하게 세션을 관리합니다.
     * 이 맵은 현재 서버 인스턴스에 연결된 세션만을 관리합니다.
     */
    private final ConcurrentHashMap<String, WebSocketSession> localSessions = new ConcurrentHashMap<>();

    /**
     * Java 객체를 JSON 문자열로 변환하기 위한 {@link ObjectMapper}입니다.
     * WebSocket 메시지를 전송하기 전에 객체를 JSON 형태로 직렬화하는 데 사용됩니다.
     */
    private final ObjectMapper objectMapper;

    /**
     * 새로운 WebSocket 세션을 로컬 맵에 추가합니다.
     *
     * @param userId 세션과 연결된 사용자의 고유 ID
     * @param session 추가할 {@link WebSocketSession} 객체
     */
    public void addLocalSession(String userId, WebSocketSession session) {
        localSessions.put(userId, session);
    }

    /**
     * 지정된 사용자 ID에 해당하는 로컬 WebSocket 세션을 조회합니다.
     *
     * @param userId 조회할 세션의 사용자 ID
     * @return 해당 사용자 ID에 대한 {@link WebSocketSession}을 포함하는 {@link Optional} 객체,
     *         세션이 존재하지 않으면 빈 {@link Optional} 객체
     */
    public Optional<WebSocketSession> getLocalSession(String userId) {
        return Optional.ofNullable(localSessions.get(userId));
    }

    /**
     * 지정된 사용자 ID에 해당하는 로컬 WebSocket 세션을 맵에서 제거합니다.
     *
     * @param userId 제거할 세션의 사용자 ID
     */
    public void removeLocalSession(String userId) {
        localSessions.remove(userId);
    }

    /**
     * 지정된 사용자 ID의 로컬 WebSocket 세션으로 메시지를 전송합니다.
     * 메시지 객체는 JSON 문자열로 변환되어 전송됩니다.
     *
     * @param userId 메시지를 전송할 대상 사용자의 ID
     * @param message 전송할 메시지 객체 (JSON으로 직렬화될 수 있는 형태)
     */
    public void sendLocalMessage(String userId, Object message) {
        // 해당 사용자 ID의 세션이 존재하면 메시지를 전송합니다.
        getLocalSession(userId).ifPresent(session -> {
            try {
                // 세션이 열려있는 상태인지 확인합니다.
                if (session.isOpen()) {
                    // 메시지 객체를 JSON 문자열로 변환하여 TextMessage로 전송합니다.
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                }
            } catch (IOException e) {
                // 메시지 전송 중 발생할 수 있는 IOException을 처리합니다.
                // 실제 애플리케이션에서는 로깅 프레임워크를 사용하여 에러를 기록하는 것이 좋습니다.
                // 예: log.error("Failed to send message to WebSocket session for user {}: {}", userId, e.getMessage());
            }
        });
    }
}
