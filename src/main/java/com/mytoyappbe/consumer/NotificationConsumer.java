package com.mytoyappbe.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.service.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka의 "notification-topic"으로부터 메시지를 소비(consume)하는 컨슈머 클래스입니다.
 * <p>
 * 수신된 메시지를 파싱하여 웹 푸시 알림을 전송하는 역할을 담당합니다.
 * {@code @Component} 어노테이션을 통해 Spring 빈으로 등록되며,
 * {@code @KafkaListener} 어노테이션을 사용하여 Kafka 토픽을 리스닝합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    /**
     * 웹 푸시 알림 전송 로직을 담당하는 서비스입니다.
     * Spring에 의해 주입됩니다.
     */
    private final WebPushService webPushService;

    /**
     * JSON 메시지를 Java 객체로 역직렬화하기 위한 {@link ObjectMapper}입니다.
     * Spring에 의해 주입됩니다.
     */
    private final ObjectMapper objectMapper;

    /**
     * Kafka의 "notification-topic"으로부터 메시지를 리스닝하는 메서드입니다.
     * <p>
     * {@code @KafkaListener} 어노테이션은 이 메서드가 Kafka 메시지를 소비하는 리스너임을 나타냅니다.
     * <ul>
     *     <li>{@code topics = "notification-topic"}: 리스닝할 Kafka 토픽의 이름을 지정합니다.</li>
     *     <li>{@code groupId = "my-group"}: 이 컨슈머가 속할 컨슈머 그룹의 ID를 지정합니다.
     *         동일한 그룹 ID를 가진 컨슈머들은 하나의 토픽의 다른 파티션들을 나누어 처리합니다.</li>
     * </ul>
     * 수신된 메시지는 {@link KafkaNotificationMessageDto}로 역직렬화된 후,
     * {@link WebPushService}를 통해 실제 웹 푸시 알림으로 전송됩니다.
     *
     * @param message Kafka로부터 수신된 JSON 형태의 알림 메시지 문자열
     */
    @KafkaListener(topics = "notification-topic", groupId = "my-group")
    public void listen(String message) {
        log.info("Received notification from Kafka: {}", message);
        try {
            // 수신된 JSON 메시지를 KafkaNotificationMessageDto 객체로 변환합니다.
            KafkaNotificationMessageDto messageDto = objectMapper.readValue(message, KafkaNotificationMessageDto.class);
            String userId = messageDto.getUserId();
            String notificationMessage = messageDto.getMessage();

            // 사용자 ID와 메시지가 유효한 경우에만 웹 푸시 알림을 전송합니다.
            if (userId != null && notificationMessage != null) {
                webPushService.sendNotificationToUser(userId, notificationMessage);
                log.info("Notification sent to user {}: {}", userId, notificationMessage);
            } else {
                log.warn("Received Kafka message missing userId or message: {}", message);
            }
        } catch (Exception e) {
            // 메시지 처리 중 예외 발생 시 에러를 로깅합니다.
            log.error("Error processing Kafka message: {}", message, e);
        }
    }
}
