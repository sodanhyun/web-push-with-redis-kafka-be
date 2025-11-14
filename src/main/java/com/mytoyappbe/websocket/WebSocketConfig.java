package com.mytoyappbe.websocket;

import com.mytoyappbe.websocket.handler.WebSocketConnectionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Spring WebSocket 설정을 담당하는 클래스입니다.
 * <p>
 * {@code @EnableWebSocket} 어노테이션을 통해 WebSocket 서버 기능을 활성화하고,
 * {@link WebSocketConfigurer} 인터페이스를 구현하여 WebSocket 핸들러를 등록하고 구성합니다.
 */
@Configuration
@EnableWebSocket // WebSocket 서버 기능을 활성화합니다.
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * WebSocket 연결 및 메시지 처리를 담당하는 핸들러입니다.
     * Spring 컨테이너에 의해 빈으로 등록된 {@link WebSocketConnectionHandler}를 주입받습니다.
     */
    private final WebSocketConnectionHandler webSocketConnectionHandler;

    /**
     * WebSocket 핸들러를 등록하고 관련 매핑 및 CORS 설정을 구성합니다.
     * <p>
     * {@code registry.addHandler()}를 사용하여 특정 URL 패턴에 {@link WebSocketConnectionHandler}를 매핑합니다.
     * <p>
     * <b>엔드포인트:</b> {@code /ws/test/{userId}}
     * 클라이언트는 이 경로로 WebSocket 연결을 시도합니다. {@code {userId}}는 경로 변수로, 연결 시 사용자 ID를 식별하는 데 사용됩니다.
     * <p>
     * <b>CORS 설정:</b> {@code .setAllowedOrigins("*")}
     * 모든 출처(Origin)에서의 WebSocket 연결을 허용하도록 CORS(Cross-Origin Resource Sharing)를 설정합니다.
     * 개발 환경에서는 편리하지만, 운영 환경에서는 보안을 위해 특정 도메인만 허용하도록 제한하는 것이 좋습니다.
     * 예: {@code .setAllowedOrigins("https://your-frontend-domain.com")}
     *
     * @param registry WebSocket 핸들러를 등록하기 위한 {@link WebSocketHandlerRegistry} 객체
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketConnectionHandler, "/ws").setAllowedOrigins("*"); // userId 제거
    }
}
