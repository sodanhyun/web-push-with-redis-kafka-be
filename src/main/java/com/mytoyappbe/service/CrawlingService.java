package com.mytoyappbe.service;

import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.service.pubsub.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 웹 크롤링 작업을 시뮬레이션하고, 그 진행 상황을 실시간으로 클라이언트에 알리는 서비스 클래스입니다.
 * <p>
 * {@code @Async} 어노테이션을 사용하여 비동기적으로 작업을 수행하며,
 * Redis를 통해 사용자의 연결 상태를 확인하고, Redis Pub/Sub을 통해 실시간 업데이트를 전송합니다.
 * 최종 완료 시에는 Kafka를 통해 푸시 알림을 발행합니다.
 */
@Service
@RequiredArgsConstructor
public class CrawlingService {

    /**
     * Redis에 현재 연결된 사용자 ID를 저장하는 Set의 키입니다.
     * 이 키를 사용하여 특정 사용자가 현재 WebSocket에 연결되어 있는지 확인합니다.
     */
    private static final String USER_SESSIONS_KEY = "ws:users";

    /**
     * Redis Pub/Sub 메시지 발행을 담당하는 서비스입니다.
     * 크롤링 진행 상황을 사용자별 토픽으로 발행하는 데 사용됩니다.
     */
    private final RedisMessagePublisher redisMessagePublisher;

    /**
     * 알림 메시지를 Kafka에 발행하는 서비스입니다.
     * 크롤링 완료 시 최종 푸시 알림을 보내는 데 사용됩니다.
     */
    private final NotificationService notificationService;

    /**
     * Redis 데이터베이스와 상호작용하기 위한 Spring의 {@link RedisTemplate}입니다.
     * 주로 {@code opsForSet()}을 사용하여 Redis Set에서 사용자 ID의 존재 여부를 확인합니다.
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 특정 사용자 ID에 대한 크롤링 작업을 비동기적으로 시작합니다.
     * <p>
     * 1. Redis를 통해 해당 사용자가 현재 WebSocket에 연결되어 있는지 확인합니다.
     * 2. 연결되어 있다면, 크롤링 작업을 시뮬레이션하며 1초마다 진행 상황을 Redis Pub/Sub으로 발행합니다.
     * 3. 작업 완료 후, 최종 완료 메시지를 Redis Pub/Sub으로 발행하고 Kafka를 통해 푸시 알림을 전송합니다.
     * <p>
     * {@code @Async} 어노테이션 덕분에 이 메서드는 별도의 스레드에서 실행되어 메인 스레드를 블로킹하지 않습니다.
     *
     * @param userId 크롤링 작업을 수행할 사용자의 ID
     */
    @Async
    public void startCrawling(String userId) {
        // Redis Set에서 userId의 존재 여부를 확인하여 사용자가 현재 연결되어 있는지 확인합니다.
        Boolean isMember = redisTemplate.opsForSet().isMember(USER_SESSIONS_KEY, userId);

        // 사용자가 연결되어 있는 경우에만 크롤링 작업을 진행합니다.
        if (isMember != null && isMember) {
            try {
                // 크롤링 진행 상황을 10단계로 시뮬레이션합니다.
                for (int i = 1; i <= 10; i++) {
                    TimeUnit.SECONDS.sleep(1); // 1초 대기하여 작업 시뮬레이션
                    Map<String, String> data = new HashMap<>();
                    data.put("title", "게시글 제목 " + i);
                    data.put("content", "게시글 내용 " + i);
                    // Redis Pub/Sub을 통해 사용자별 토픽으로 진행 상황 메시지를 발행합니다.
                    redisMessagePublisher.publish("ws:user:" + userId, data);
                    System.out.println("[서버->클라이언트] title: " + data.get("title") + " content: " + data.get("content") + "전송 완료!");
                }
                // 크롤링 완료 메시지를 발행합니다.
                Map<String, String> completionMessage = new HashMap<>();
                completionMessage.put("status", "complete");
                redisMessagePublisher.publish("ws:user:" + userId, completionMessage);

                // 크롤링 완료 후, Kafka를 통해 사용자에게 웹 푸시 알림을 전송합니다.
                notificationService.sendNotification(new KafkaNotificationMessageDto(userId, "크롤링이 완료되었습니다."));

            } catch (InterruptedException e) {
                // 스레드 인터럽트 발생 시 현재 스레드의 인터럽트 상태를 다시 설정합니다.
                Thread.currentThread().interrupt();
                log.error("Crawling process for user {} was interrupted.", userId, e);
            }
        }
    }
}
