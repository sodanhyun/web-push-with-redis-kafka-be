package com.mytoyappbe.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka의 "notification-topic"으로 전송되거나 수신되는 알림 메시지의 데이터 전송 객체(DTO)입니다.
 * <p>
 * 이 DTO는 알림 메시지 내용을 캡슐화하여 Kafka 메시지의 구조를 정의합니다.
 * Lombok의 {@code @Data}, {@code @NoArgsConstructor}, {@code @AllArgsConstructor} 어노테이션을 사용하여
 * 보일러플레이트 코드를 줄였습니다.
 */
@Data // Getter, Setter, toString, equals, hashCode 메서드를 자동으로 생성합니다.
@NoArgsConstructor // 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
public class KafkaNotificationMessageDto {
    /**
     * 사용자에게 전송될 알림 메시지의 내용입니다.
     */
    private String message;
}
