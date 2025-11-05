package com.mytoyappbe.controller;

import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 전송 관련 REST API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 알림 메시지 요청을 받아 Kafka를 통해 비동기적으로 알림을 발행합니다.
 */
@RestController
@RequestMapping("/api/notifications") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/notifications" 경로를 기본으로 합니다.
@RequiredArgsConstructor
public class NotificationController {

    /**
     * 알림 메시지를 Kafka에 발행하는 서비스입니다.
     * Spring에 의해 주입됩니다.
     */
    private final NotificationService notificationService;

    /**
     * 알림 메시지 전송 요청을 받아 Kafka에 발행하는 엔드포인트입니다.
     * <p>
     * {@code @PostMapping} 어노테이션은 HTTP POST 요청을 "/api/notifications" 경로로 매핑합니다.
     * {@code @RequestBody KafkaNotificationMessageDto messageDto}는 요청 본문에 포함된 JSON 데이터를
     * {@link KafkaNotificationMessageDto} 객체로 자동 변환하여 받습니다.
     * 이 메시지는 {@link NotificationService}를 통해 Kafka의 "notification-topic"으로 발행됩니다.
     *
     * @param messageDto 전송할 알림 메시지 정보 (사용자 ID, 메시지 내용 등 포함)
     * @return 알림 전송 성공 메시지
     */
    @PostMapping
    public String sendNotification(@RequestBody KafkaNotificationMessageDto messageDto) {
        notificationService.sendNotification(messageDto);
        return "Notification sent successfully!";
    }
}
