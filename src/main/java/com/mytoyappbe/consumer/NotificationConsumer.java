package com.mytoyappbe.consumer;

import com.mytoyappbe.service.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final WebPushService webPushService;

    @KafkaListener(topics = "notification-topic", groupId = "my-group")
    public void listen(String message) {
        log.info("Received notification from Kafka: {}", message);
        webPushService.sendNotificationToAll(message);
    }
}
