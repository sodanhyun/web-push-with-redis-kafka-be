/**
 * @file WebSocketConfig.java
 * @description Spring WebSocket STOMP 설정을 담당하는 클래스입니다.
 *              STOMP 엔드포인트 및 메시지 브로커를 구성하여 실시간 통신 기능을 제공합니다.
 */

package com.mytoyappbe.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @class WebSocketConfig
 * @description Spring STOMP over WebSocket 설정을 정의하는 클래스입니다.
 *              클라이언트가 연결할 STOMP 엔드포인트를 등록하고, 메시지 브로커를 구성합니다.
 *              `spring-session-data-redis`와 함께 사용하여 분산 환경에서 메시지 동기화를 지원합니다.
 */
@Configuration // Spring 설정 클래스임을 나타냅니다.
@EnableWebSocketMessageBroker // STOMP over WebSocket 서버 기능을 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * @method registerStompEndpoints
     * @description STOMP 엔드포인트를 등록합니다.
     *              클라이언트는 이 엔드포인트로 WebSocket 연결을 시작합니다.
     *              `/ws` 엔드포인트를 SockJS 폴백 옵션과 함께 등록하여 WebSocket을 지원하지 않는 브라우저에서도 연결을 가능하게 합니다.
     * @param {StompEndpointRegistry} registry - STOMP 엔드포인트를 등록하기 위한 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트가 WebSocket 연결을 시작할 엔드포인트
                .setAllowedOriginPatterns("*") // 모든 출처(Origin)에서의 연결 허용 (운영 환경에서는 특정 도메인으로 제한)
                .withSockJS(); // SockJS 폴백 옵션 활성화
    }

    /**
     * @method configureMessageBroker
     * @description 메시지 브로커를 구성합니다.
     *              클라이언트가 메시지를 발행할 때 사용하는 접두사(`setApplicationDestinationPrefixes`)와
     *              메시지를 구독할 때 사용하는 접두사(`enableSimpleBroker`)를 정의합니다.
     *              `spring-session-data-redis`가 활성화되어 있으면 Redis를 메시지 브로커의 백플레인으로 활용하여
     *              여러 인스턴스 간 메시지 동기화를 자동으로 처리합니다.
     * @param {MessageBrokerRegistry} registry - 메시지 브로커를 구성하기 위한 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 서버의 @MessageMapping 핸들러로 메시지를 보낼 때 사용하는 접두사입니다.
        // 예: 클라이언트가 /app/hello로 메시지를 보내면 @MessageMapping("/hello") 핸들러로 라우팅됩니다.
        registry.setApplicationDestinationPrefixes("/app");

        // 인메모리 메시지 브로커를 활성화합니다.
        // `/topic`으로 시작하는 메시지는 모든 구독자에게 브로드캐스트됩니다.
        // `/queue`로 시작하는 메시지는 특정 사용자에게 전송됩니다.
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {10000, 10000}) // 클라이언트와 서버 간 하트비트 설정 (밀리초)
                .setCacheLimit(1024 * 1024); // 브로커가 메시지를 캐시할 수 있는 최대 크기 (바이트)

        // 사용자별 메시지를 보낼 때 사용하는 접두사입니다.
        // `/user/{userId}/queue/messages`와 같은 경로로 메시지를 보낼 수 있습니다.
        registry.setUserDestinationPrefix("/user");
    }
}