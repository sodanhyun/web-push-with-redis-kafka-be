package com.mytoyappbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_TOPIC = "notification-topic";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendNotification(KafkaNotificationMessageDto messageDto) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageDto);
            System.out.println("Sending notification: " + jsonMessage);
            kafkaTemplate.send(NOTIFICATION_TOPIC, jsonMessage);
        } catch (JsonProcessingException e) {
            // Handle exception, e.g., log it
            e.printStackTrace();
        }
    }
}
