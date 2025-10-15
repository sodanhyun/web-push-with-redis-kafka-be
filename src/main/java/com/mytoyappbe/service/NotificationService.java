package com.mytoyappbe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_TOPIC = "notification-topic";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendNotification(String message) {
        System.out.println("Sending notification: " + message);
        kafkaTemplate.send(NOTIFICATION_TOPIC, message);
    }
}
