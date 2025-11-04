package com.mytoyappbe.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.service.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final WebPushService webPushService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notification-topic", groupId = "my-group")
    public void listen(String message) {
        log.info("Received notification from Kafka: {}", message);
        try {
            KafkaNotificationMessageDto messageDto = objectMapper.readValue(message, KafkaNotificationMessageDto.class);
            String userId = messageDto.getUserId();
            String notificationMessage = messageDto.getMessage();

            if (userId != null && notificationMessage != null) {
                webPushService.sendNotificationToUser(userId, notificationMessage);
                log.info("Notification sent to user {}: {}", userId, notificationMessage);
            } else {
                log.warn("Received Kafka message missing userId or message: {}", message);
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
        }
    }
}
