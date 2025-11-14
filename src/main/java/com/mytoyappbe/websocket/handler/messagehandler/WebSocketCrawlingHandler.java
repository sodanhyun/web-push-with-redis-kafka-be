package com.mytoyappbe.websocket.handler.messagehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.websocket.handler.AbstractMessageHandler;
import com.mytoyappbe.websocket.manager.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class WebSocketCrawlingHandler extends AbstractMessageHandler {

    private final WebSocketSessionManager webSocketSessionManager;
    private final ObjectMapper objectMapper;

    public WebSocketCrawlingHandler(WebSocketSessionManager webSocketSessionManager, ObjectMapper objectMapper) {
        super("ws:crawling");
        this.webSocketSessionManager = webSocketSessionManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(String channel, String messageBody) {
        try {
            // String userId = extractUserId(channel); // userId 추출 로직 제거
            Map<String, Object> data = objectMapper.readValue(messageBody, Map.class); // Map<String, String>에서 Map<String, Object>로 변경 (progress 필드 때문에)
            webSocketSessionManager.sendAllLocalSessions(data); // 모든 로컬 세션에 브로드캐스트
        } catch (IOException e) {
            log.error("Error processing message from Redis Pub/Sub for channel: {}", channel, e);
        }
    }
}
