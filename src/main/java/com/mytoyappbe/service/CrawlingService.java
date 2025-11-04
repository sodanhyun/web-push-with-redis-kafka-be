package com.mytoyappbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.manager.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
        private final NotificationService notificationService;
    
        @Async
        public void startCrawling(String userId) {
            sessionManager.getSession(userId).ifPresent(session -> {
                try {
                    for (int i = 1; i <= 10; i++) {
                        TimeUnit.SECONDS.sleep(1);
                        Map<String, String> data = new HashMap<>();
                        data.put("title", "게시글 제목 " + i);
                        data.put("content", "게시글 내용 " + i);
                        sendWebSocketMessage(session, data);
                        System.out.println("[서버->클라이언트] title: " + data.get("title") + " content: " + data.get("content") + "전송 완료!");
                    }
                    Map<String, String> completionMessage = new HashMap<>();
                    completionMessage.put("status", "complete");
                    sendWebSocketMessage(session, completionMessage);
    
                    // Send web push notification to the specific user
                    notificationService.sendNotification(new KafkaNotificationMessageDto(userId, "크롤링이 완료되었습니다."));
    
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

    private void sendWebSocketMessage(WebSocketSession session, Object data) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }
}
