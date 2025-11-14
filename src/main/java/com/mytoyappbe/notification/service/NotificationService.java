/**
 * @file NotificationService.java
 * @description 알림 메시지를 Kafka 토픽에 발행(publish)하는 서비스 클래스입니다.
 *              이 서비스는 {@link KafkaNotificationMessageDto} 객체를 JSON 문자열로 변환하여
 *              미리 정의된 Kafka 토픽으로 전송하는 역할을 담당합니다.
 */

package com.mytoyappbe.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.notification.config.KafkaTopicConfig;
import com.mytoyappbe.notification.dto.KafkaNotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @class NotificationService
 * @description 알림 메시지를 Kafka 토픽에 발행하는 서비스입니다.
 *              `KafkaNotificationMessageDto` 객체를 JSON 문자열로 변환하여
 *              지정된 Kafka 토픽으로 전송하는 기능을 제공합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@Service // Spring 서비스 컴포넌트임을 나타냅니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class NotificationService {

    /**
     * 알림 메시지를 발행할 Kafka 토픽의 이름입니다.
     * {@link KafkaTopicConfig}에서 정의된 토픽과 일치해야 합니다.
     */
    private static final String NOTIFICATION_TOPIC = "notification-topic";

    /**
     * Kafka 메시지를 전송하기 위한 Spring의 {@link KafkaTemplate}입니다.
     * 이 템플릿을 통해 지정된 토픽으로 메시지를 쉽게 발행할 수 있습니다.
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Java 객체를 JSON 문자열로 변환하기 위한 {@link ObjectMapper}입니다.
     * Kafka 메시지로 전송하기 전에 {@link KafkaNotificationMessageDto} 객체를 JSON 형태로 직렬화하는 데 사용됩니다.
     */
    private final ObjectMapper objectMapper;

    /**
     * @method sendNotification
     * @description {@link KafkaNotificationMessageDto} 객체를 JSON 문자열로 직렬화하여 Kafka 토픽에 발행합니다.
     *              메시지 발행 중 {@link JsonProcessingException}이 발생하면 예외를 처리하고 로깅합니다.
     * @param {String} userId - Kafka 메시지의 키로 사용될 사용자 ID
     * @param {KafkaNotificationMessageDto} messageDto - Kafka에 발행할 알림 메시지 정보를 담은 DTO
     */
    public void sendNotification(String userId, KafkaNotificationMessageDto messageDto) {
        try {
            // DTO 객체를 JSON 문자열로 변환합니다.
            String jsonMessage = objectMapper.writeValueAsString(messageDto);
            log.info("Sending notification to Kafka for userId: {}, message: {}", userId, jsonMessage);
            // KafkaTemplate을 사용하여 지정된 토픽으로 메시지를 전송합니다. userId를 메시지 키로 사용합니다.
            kafkaTemplate.send(NOTIFICATION_TOPIC, userId, jsonMessage);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 중 발생할 수 있는 예외를 처리하고 에러를 로깅합니다.
            log.error("Error serializing notification message to JSON for userId: {}", userId, e);
        }
    }
}