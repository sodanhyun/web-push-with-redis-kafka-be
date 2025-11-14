/**
 * @file CrawlingService.java
 * @description 웹 크롤링 작업을 시뮬레이션하고, 그 진행 상황을 실시간으로 클라이언트에 알리는 서비스 클래스입니다.
 *              `@Async` 어노테이션을 사용하여 비동기적으로 작업을 수행하며,
 *              STOMP over WebSocket을 통해 특정 사용자에게 진행 상황을 전송하고,
 *              최종 완료 시에는 Kafka를 통해 푸시 알림을 발행합니다.
 */

package com.mytoyappbe.crawling.service;

import com.mytoyappbe.notification.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.crawling.dto.CrawlingProgressMessageDto;
import com.mytoyappbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @class CrawlingService
 * @description 웹 크롤링 작업을 시뮬레이션하고, 그 진행 상황을 실시간으로 클라이언트에게 알리는 서비스입니다.
 *              비동기적으로 크롤링 작업을 수행하며, STOMP over WebSocket을 통해 사용자별 진행 상황을 업데이트하고,
 *              작업 완료 시 푸시 알림을 전송합니다.
 */
@Service // Spring 서비스 컴포넌트임을 나타냅니다.
@Slf4j // 로깅을 위한 Lombok 어노테이션
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class CrawlingService {

    // Redis에 저장된 웹 푸시 구독 정보를 조회하기 위한 해시 키
    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user";

    // STOMP over WebSocket을 통해 클라이언트에게 메시지를 전송하는 템플릿
    private final SimpMessagingTemplate messagingTemplate;

    // 알림 메시지를 Kafka에 발행하는 서비스
    private final NotificationService notificationService;

    // Redis 데이터베이스와 상호작용하기 위한 템플릿
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * @method startCrawling
     * @description 특정 사용자 ID에 대한 크롤링 작업을 비동기적으로 시작합니다.
     *              크롤링 진행 상황을 시뮬레이션하고, 주기적으로 STOMP over WebSocket을 통해
     *              해당 사용자에게 진행 상황 메시지를 전송합니다. 작업 완료 시 푸시 알림을 보냅니다.
     * @param {String} userId - 크롤링 작업을 수행할 사용자의 ID
     */
    @Async // 이 메서드를 비동기적으로 실행하도록 지정합니다.
    public void startCrawling(String userId) {
        try {
            // 크롤링 진행 상황을 10단계로 시뮬레이션합니다.
            for (int i = 1; i <= 10; i++) {
                TimeUnit.SECONDS.sleep(1); // 1초 대기하여 작업 시뮬레이션
                
                // 크롤링 진행 상황 메시지 DTO를 생성합니다.
                CrawlingProgressMessageDto progressMessage = CrawlingProgressMessageDto.builder()
                        .userId(userId)
                        .title("게시글 제목 " + i)
                        .content("게시글 내용 " + i)
                        .status("IN_PROGRESS")
                        .progress(i * 10)
                        .build();
                
                // SimpMessagingTemplate을 사용하여 특정 사용자에게 STOMP 메시지를 전송합니다.
                // 목적지는 `/user/{userId}/queue/crawling-progress` 형태로 구성됩니다.
                messagingTemplate.convertAndSendToUser(userId, "/queue/crawling-progress", progressMessage);
                System.out.println("[서버->클라이언트] title: " + progressMessage.getTitle() + " content: " + progressMessage.getContent() + "전송 완료!");
            }

            // 크롤링 완료 메시지를 생성하고 전송합니다.
            CrawlingProgressMessageDto completionMessage = CrawlingProgressMessageDto.builder()
                    .userId(userId)
                    .title("크롤링 완료")
                    .content("모든 게시글 크롤링이 완료되었습니다.")
                    .status("COMPLETED")
                    .progress(100)
                    .build();
            messagingTemplate.convertAndSendToUser(userId, "/queue/crawling-progress", completionMessage);

            // 푸시 알림 전송 로직:
            // Redis에 해당 사용자의 푸시 구독 정보가 있는지 확인합니다.
            Object subscription = redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
            if (subscription != null) {
                // 구독 정보가 있을 경우에만 Kafka를 통해 푸시 알림을 전송합니다.
                log.info("Push subscription found for user {}. Sending notification.", userId);
                notificationService.sendNotification(userId, new KafkaNotificationMessageDto("크롤링이 완료되었습니다."));
            } else {
                // 구독 정보가 없으면 알림을 보내지 않고 경고 로그를 남깁니다.
                log.warn("User {} has no push subscription. Skipping notification.", userId);
            }

        } catch (InterruptedException e) {
            // 스레드 인터럽트 발생 시 현재 스레드의 인터럽트 상태를 다시 설정하고 에러를 로깅합니다.
            Thread.currentThread().interrupt();
            log.error("Crawling process for user {} was interrupted.", userId, e);
        }
    }
}