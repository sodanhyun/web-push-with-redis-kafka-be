package com.mytoyappbe.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.manager.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Redis Pub/Sub을 통해 수신된 WebSocket 메시지를 처리하는 {@link MessageHandler} 구현체입니다.
 * "ws:user:" 패턴의 채널에서 메시지를 수신하며, 해당 메시지를 파싱하여
 * 로컬 {@link WebSocketSessionManager}를 통해 클라이언트 WebSocket 세션으로 전달합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageHandler implements MessageHandler {

    /**
     * 이 핸들러가 처리하는 채널 패턴의 접두사입니다.
     * 예: "ws:user:userId" 형태의 채널을 처리합니다.
     */
    private static final String HANDLER_PREFIX = "ws:user:";

    /**
     * 로컬 WebSocket 세션을 관리하고 메시지를 전송하는 매니저입니다.
     */
    private final WebSocketSessionManager webSocketSessionManager;

    /**
     * JSON 메시지를 객체로 역직렬화하기 위한 ObjectMapper입니다.
     */
    private final ObjectMapper objectMapper;

    /**
     * 주어진 채널이 이 핸들러가 처리할 수 있는 "ws:user:" 패턴인지 확인합니다.
     *
     * @param channel 메시지가 수신된 Redis 채널(토픽)
     * @return 채널이 "ws:user:"로 시작하면 true, 그렇지 않으면 false
     */
    @Override
    public boolean canHandle(String channel) {
        return channel.startsWith(HANDLER_PREFIX);
    }

    /**
     * "ws:user:" 패턴의 채널에서 수신된 메시지를 처리합니다.
     * 채널에서 사용자 ID를 추출하고, 메시지 본문을 파싱하여 해당 사용자에게 WebSocket 메시지를 전송합니다.
     *
     * @param channel 메시지가 수신된 Redis 채널(토픽)
     * @param messageBody 수신된 메시지의 본문 (JSON String 형태)
     */
    @Override
    public void handle(String channel, String messageBody) {
        try {
            String userId = extractUserIdFromChannel(channel);
            // 메시지 본문을 Map<String, String> 형태로 역직렬화합니다.
            Map<String, String> data = objectMapper.readValue(messageBody, Map.class);
            webSocketSessionManager.sendLocalMessage(userId, data);
        } catch (IOException e) {
            log.error("Error processing message from Redis Pub/Sub for channel: {}", channel, e);
        }
    }

    /**
     * 채널 이름에서 사용자 ID를 추출합니다.
     * 예: "ws:user:123" -> "123"
     *
     * @param channel 사용자 ID가 포함된 채널 이름
     * @return 추출된 사용자 ID
     */
    private String extractUserIdFromChannel(String channel) {
        return channel.substring(HANDLER_PREFIX.length());
    }
}
