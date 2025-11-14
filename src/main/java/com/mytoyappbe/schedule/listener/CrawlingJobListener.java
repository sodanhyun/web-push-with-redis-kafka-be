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
 * Spring Batch Job의 실행 완료 후 이벤트를 처리하는 리스너입니다.
 * Job이 성공적으로 완료되면, 해당 Job을 요청한 사용자에게 푸시 알림을 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingJobListener implements JobExecutionListener {

    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user";

    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate<String, Object>로 변경


    /**
     * Job 실행 전에 호출됩니다. 현재는 특별한 로직이 없습니다.
     * @param jobExecution 현재 Job 실행에 대한 정보
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job {} started.", jobExecution.getJobInstance().getJobName());
    }

    /**
     * Job 실행 완료 후에 호출됩니다.
     * Job이 성공적으로 완료되면, Job Parameters에서 userId를 추출하여 사용자에게 알림을 보냅니다.
     * @param jobExecution 현재 Job 실행에 대한 정보
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job {} completed successfully.", jobExecution.getJobInstance().getJobName());
            String userId = jobExecution.getJobParameters().getString("userId");
            if (userId != null) {
                // Redis에 해당 사용자의 푸시 구독 정보가 있는지 확인합니다.
                Object subscription = redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
                if (subscription != null) {
                    log.info("Sending completion notification to user: {}", userId);
                    notificationService.sendNotification(userId, new KafkaNotificationMessageDto("크롤링 작업이 성공적으로 완료되었습니다!"));
                } else {
                    log.warn("User {} has no push subscription. Skipping notification.", userId);
                }
            } else {
                log.warn("Job {} completed, but userId parameter was not found. Cannot send notification.", jobExecution.getJobInstance().getJobName());
            }
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Job {} failed with status {}. Error: {}",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getStatus(),
                    jobExecution.getAllFailureExceptions());
            String userId = jobExecution.getJobParameters().getString("userId");
            if (userId != null) {
                // Redis에 해당 사용자의 푸시 구독 정보가 있는지 확인합니다.
                Object subscription = redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
                if (subscription != null) {
                    log.info("Sending failure notification to user: {}", userId);
                    notificationService.sendNotification(userId, new KafkaNotificationMessageDto("크롤링 작업이 실패했습니다. 다시 시도해주세요."));
                } else {
                    log.warn("User {} has no push subscription. Skipping failure notification.", userId);
                }
            } else {
                log.warn("Job {} failed, but userId parameter was not found. Cannot send failure notification.", jobExecution.getJobInstance().getJobName());
            }
        }
    }
}
