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
@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingJobListener implements JobExecutionListener {

    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user";

    private final NotificationService notificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job {} started.", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        BatchStatus status = jobExecution.getStatus();

        if (status == BatchStatus.COMPLETED) {
            log.info("Job {} completed successfully.", jobName);
        } else if (status == BatchStatus.FAILED) {
            log.error("Job {} failed with status {}. Error: {}",
                    jobName,
                    status,
                    jobExecution.getAllFailureExceptions());
        }
    }
}