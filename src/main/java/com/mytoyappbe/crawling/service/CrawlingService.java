package com.mytoyappbe.crawling.service;

import com.mytoyappbe.notification.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.crawling.dto.CrawlingProgressMessageDto;
import com.mytoyappbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate; // RedisTemplate 임포트 추가
import org.springframework.messaging.simp.SimpMessagingTemplate; // SimpMessagingTemplate 임포트 추가
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 웹 크롤링 작업을 시뮬레이션하고, 그 진행 상황을 실시간으로 클라이언트에 알리는 서비스 클래스입니다.
 * <p>
 * {@code @Async} 어노테이션을 사용하여 비동기적으로 작업을 수행하며,
 * Redis를 통해 사용자의 연결 상태를 확인하고, Redis Pub/Sub을 통해 실시간 업데이트를 전송합니다.
 * 최종 완료 시에는 Kafka를 통해 푸시 알림을 발행합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlingService {

    // Redis 관련 필드 제거 (USER_SESSIONS_KEY)
    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user"; // 이 필드는 푸시 알림 로직에서 사용되므로 유지

    // RedisMessagePublisher 대신 SimpMessagingTemplate 사용
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 알림 메시지를 Kafka에 발행하는 서비스입니다.
     * 크롤링 완료 시 최종 푸시 알림을 보내는 데 사용됩니다.
     */
    private final NotificationService notificationService;

    // RedisTemplate<String, String> redisTemplate; 필드를 다시 추가합니다.
    private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate<String, Object>로 변경

    /**
     * 특정 사용자 ID에 대한 크롤링 작업을 비동기적으로 시작합니다.
     * <p>
     * 이 메서드는 실제 크롤링 로직을 시뮬레이션하며, 진행 상황을 주기적으로 WebSocket을 통해
     * {@code /topic/crawling-progress} 경로로 클라이언트에 전송합니다.
     *
     * @param userId 크롤링 작업을 수행할 사용자의 ID
     */
    @Async
    public void startCrawling(String userId) {
        try {
            // Redis Set에서 userId의 존재 여부를 확인하는 로직 제거

            // 크롤링 진행 상황을 10단계로 시뮬레이션합니다.
            for (int i = 1; i <= 10; i++) {
                TimeUnit.SECONDS.sleep(1); // 1초 대기하여 작업 시뮬레이션
                // 사용자가 WebSocket에 연결되어 있을 때만 실시간 진행 상황을 보냅니다.
                CrawlingProgressMessageDto progressMessage = CrawlingProgressMessageDto.builder()
                        .userId(userId) // userId 설정
                        .title("게시글 제목 " + i)
                        .content("게시글 내용 " + i)
                        .status("IN_PROGRESS")
                        .progress(i * 10) // progress 설정
                        .build();
                // SimpMessagingTemplate을 사용하여 STOMP 토픽으로 메시지 전송
                messagingTemplate.convertAndSend("/topic/crawling-progress", progressMessage);
                System.out.println("[서버->클라이언트] title: " + progressMessage.getTitle() + " content: " + progressMessage.getContent() + "전송 완료!");

            }

            // 크롤링 완료 메시지를 발행합니다.
            CrawlingProgressMessageDto completionMessage = CrawlingProgressMessageDto.builder()
                    .userId(userId) // userId 설정
                    .title("크롤링 완료")
                    .content("모든 게시글 크롤링이 완료되었습니다.")
                    .status("COMPLETED")
                    .progress(100) // progress 설정
                    .build();
            // SimpMessagingTemplate을 사용하여 STOMP 토픽으로 메시지 전송
            messagingTemplate.convertAndSend("/topic/crawling-progress", completionMessage);


            // --- 푸시 알림 전송 로직 ---
            // Redis에 해당 사용자의 푸시 구독 정보가 있는지 확인하여 레이스 컨디션을 방지합니다.
            Object subscription = redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
            if (subscription != null) {
                // 구독 정보가 있을 경우에만 Kafka를 통해 푸시 알림을 전송합니다.
                log.info("Push subscription found for user {}. Sending notification.", userId);
                notificationService.sendNotification(userId, new KafkaNotificationMessageDto("크롤링이 완료되었습니다."));
            } else {
                // 구독 정보가 없으면 알림을 보내지 않고 로그를 남깁니다.
                log.warn("User {} has no push subscription. Skipping notification.", userId);
            }

        } catch (InterruptedException e) {
            // 스레드 인터럽트 발생 시 현재 스레드의 인터럽트 상태를 다시 설정합니다.
            Thread.currentThread().interrupt();
            log.error("Crawling process for user {} was interrupted.", userId, e);
        }
    }
}
