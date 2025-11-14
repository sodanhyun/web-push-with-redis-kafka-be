/**
 * @file CrawlingJobListener.java
 * @description Spring Batch Job의 실행 완료 후 이벤트를 처리하는 리스너입니다.
 *              Job이 성공적으로 완료되거나 실패하면, 해당 Job을 요청한 사용자에게 푸시 알림을 전송합니다.
 */

package com.mytoyappbe.schedule.listener;

import com.mytoyappbe.notification.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @class CrawlingJobListener
 * @description Spring Batch Job의 생명주기 이벤트를 수신하고 처리하는 리스너입니다.
 *              특히 Job 완료 후 사용자에게 알림을 전송하는 역할을 수행합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@Component // Spring 컨테이너에 빈으로 등록합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class CrawlingJobListener implements JobExecutionListener {

    // Redis에 저장된 웹 푸시 구독 정보를 조회하기 위한 해시 키
    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user";

    private final NotificationService notificationService; // 알림 메시지를 Kafka에 발행하는 서비스
    private final RedisTemplate<String, Object> redisTemplate; // Redis 데이터베이스와 상호작용하기 위한 템플릿

    /**
     * @method beforeJob
     * @description Job 실행 전에 호출되는 콜백 메서드입니다.
     *              현재는 Job 시작 로그를 기록하는 역할만 수행합니다.
     * @param {JobExecution} jobExecution - 현재 Job 실행에 대한 정보
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job {} started.", jobExecution.getJobInstance().getJobName());
    }

    /**
     * @method afterJob
     * @description Job 실행 완료 후에 호출되는 콜백 메서드입니다.
     *              Job의 상태(성공/실패)에 따라 사용자에게 푸시 알림을 전송합니다.
     * @param {JobExecution} jobExecution - 현재 Job 실행에 대한 정보
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        // Job이 성공적으로 완료된 경우
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job {} completed successfully.", jobExecution.getJobInstance().getJobName());
            // Job Parameters에서 userId를 추출합니다.
            String userId = jobExecution.getJobParameters().getString("userId");
            if (userId != null) {
                // Redis에 해당 사용자의 푸시 구독 정보가 있는지 확인합니다.
                Object subscription = redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
                if (subscription != null) {
                    // 구독 정보가 있다면 사용자에게 완료 알림을 전송합니다.
                    log.info("Sending completion notification to user: {}", userId);
                    notificationService.sendNotification(userId, new KafkaNotificationMessageDto("크롤링 작업이 성공적으로 완료되었습니다!"));
                } else {
                    // 구독 정보가 없으면 경고 로그를 남깁니다.
                    log.warn("User {} has no push subscription. Skipping notification.", userId);
                }
            } else {
                log.warn("Job {} completed, but userId parameter was not found. Cannot send notification.", jobExecution.getJobInstance().getJobName());
            }
        }
        // Job이 실패한 경우
        else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Job {} failed with status {}. Error: {}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getStatus(),
                    jobExecution.getAllFailureExceptions());
            // Job Parameters에서 userId를 추출합니다.
            String userId = jobExecution.getJobParameters().getString("userId");
            if (userId != null) {
                // Redis에 해당 사용자의 푸시 구독 정보가 있는지 확인합니다.
                Object subscription = redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
                if (subscription != null) {
                    // 구독 정보가 있다면 사용자에게 실패 알림을 전송합니다.
                    log.info("Sending failure notification to user: {}", userId);
                    notificationService.sendNotification(userId, new KafkaNotificationMessageDto("크롤링 작업이 실패했습니다. 다시 시도해주세요."));
                } else {
                    // 구독 정보가 없으면 경고 로그를 남깁니다.
                    log.warn("User {} has no push subscription. Skipping failure notification.", userId);
                }
            } else {
                log.warn("Job {} failed, but userId parameter was not found. Cannot send failure notification.", jobExecution.getJobInstance().getJobName());
            }
        }
    }
}