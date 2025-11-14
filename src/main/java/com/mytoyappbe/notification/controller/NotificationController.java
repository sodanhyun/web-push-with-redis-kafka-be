/**
 * @file NotificationController.java
 * @description 알림 전송 관련 REST API 요청을 처리하는 컨트롤러입니다.
 *              클라이언트로부터 알림 메시지 요청을 받아 Kafka를 통해 비동기적으로 알림을 발행합니다.
 */

package com.mytoyappbe.notification.controller;

import com.mytoyappbe.notification.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class NotificationController
 * @description 알림 전송 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 *              `/api/notifications` 경로로 들어오는 요청을 매핑합니다.
 */
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/notifications") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/notifications" 경로를 기본으로 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class NotificationController {

    private final NotificationService notificationService; // 알림 메시지를 Kafka에 발행하는 서비스

    /**
     * @method sendNotification
     * @description 클라이언트로부터 알림 메시지 전송 요청을 받아 Kafka에 발행하는 엔드포인트입니다.
     *              `@AuthenticationPrincipal`을 통해 현재 로그인한 사용자의 ID를 가져와 알림 서비스에 전달합니다.
     *              요청 본문의 {@link KafkaNotificationMessageDto}를 사용하여 알림 메시지 내용을 받습니다.
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보
     * @param {KafkaNotificationMessageDto} messageDto - 전송할 알림 메시지 정보 (메시지 내용 포함)
     * @returns {String} 알림 전송 성공 메시지
     */
    @PostMapping // HTTP POST 요청을 "/api/notifications" 경로에 매핑합니다.
    public String sendNotification(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestBody KafkaNotificationMessageDto messageDto) {
        // NotificationService를 호출하여 Kafka에 알림 메시지를 발행합니다.
        // userDetails.getUsername()을 통해 현재 로그인한 사용자의 ID를 가져와 메시지의 키로 사용합니다.
        notificationService.sendNotification(userDetails.getUsername(), messageDto);
        return "Notification sent successfully!"; // 성공 메시지 반환
    }
}