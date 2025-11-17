/**
 * @file ScheduleManager.java
 * @description 크롤링 작업을 동적으로 스케줄링하고 관리하는 컴포넌트입니다.
 *              {@link ThreadPoolTaskScheduler}를 사용하여 Cron 표현식에 따라 작업을 예약하고,
 *              Spring Batch의 {@link JobLauncher}를 통해 배치 작업을 실행합니다.
 *              예약된 작업의 취소 및 재스케줄링 기능을 제공하여 런타임에 스케줄을 유연하게 제어할 수 있도록 합니다.
 */
package com.mytoyappbe.schedule.service;

import com.mytoyappbe.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @class ScheduleManager
 * @description 크롤링 작업을 동적으로 스케줄링하고 관리하는 컴포넌트입니다.
 *              `ThreadPoolTaskScheduler`를 사용하여 Cron 표현식에 따라 작업을 예약하고,
 *              `JobLauncher`를 통해 Spring Batch 작업을 실행합니다.
 *              예약된 작업의 취소 및 재스케줄링 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleManager {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator;

    /**
     * 현재 스케줄링된 작업들을 관리하는 맵입니다.
     * `Schedule`의 ID를 키로, 예약된 작업의 `ScheduledFuture` 객체를 값으로 가집니다.
     * `ScheduledFuture`를 통해 예약된 작업을 취소하거나 상태를 확인할 수 있습니다.
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * @method scheduleJob
     * @description 새로운 크롤링 작업을 스케줄링합니다.
     *              이전에 동일한 ID로 스케줄링된 작업이 있다면 먼저 취소하고 새로 예약합니다.
     *              작업은 `CronTrigger`에 정의된 Cron 표현식에 따라 주기적으로 실행됩니다.
     *              실행 시 Spring Batch `Job`을 `JobLauncher`를 통해 시작합니다.
     * @param jobSchedule - 스케줄링할 크롤링 작업 정보 (ID, userId, cronExpression, jobName 등 포함)
     */
    public void scheduleJob(Schedule jobSchedule) {
        // 기존에 스케줄링된 작업이 있다면 취소합니다.
        cancelJob(jobSchedule.getId());

        // 스케줄러에 의해 실행될 실제 작업 (Runnable)을 정의합니다.
        Runnable task = () -> {
            try {
                // JobLocator를 통해 실행할 Spring Batch Job 빈을 이름으로 가져옵니다.
                Job job = jobLocator.getJob(jobSchedule.getJobName());

                // Job Parameters를 빌드합니다.
                // Job Parameters는 JobInstance를 고유하게 식별하고, Job 실행에 필요한 데이터를 전달합니다.
                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
                jobParametersBuilder.addString("userId", jobSchedule.getUserId());
                // "run.date" 파라미터는 매번 다른 JobInstance를 생성하기 위해 현재 시각을 추가합니다.
                jobParametersBuilder.addDate("run.date", new Date());
                // TODO: jobSchedule.getJobParameters() (JSON 문자열)를 파싱하여 추가 파라미터를 설정할 수 있습니다.

                // JobLauncher를 통해 Spring Batch Job을 실행합니다.
                jobLauncher.run(job, jobParametersBuilder.toJobParameters());
                log.info("Scheduled job {} (userId: {}) executed successfully.", jobSchedule.getId(), jobSchedule.getUserId());
            } catch (JobExecutionException e) {
                // Spring Batch Job 실행 중 예외 발생 시 처리
                log.error("Failed to execute scheduled job {} (userId: {}): {}", jobSchedule.getId(), jobSchedule.getUserId(), e.getMessage());
                // TODO: Job 상태를 FAILED로 업데이트하는 로직 추가
            } catch (Exception e) {
                // 그 외 예상치 못한 예외 발생 시 처리
                log.error("An unexpected error occurred during job execution for job {} (userId: {}): {}", jobSchedule.getId(), jobSchedule.getUserId(), e.getMessage());
                // TODO: Job 상태를 FAILED로 업데이트하는 로직 추가
            }
        };

        // Cron 표현식에 따라 작업을 스케줄링하고 ScheduledFuture 객체를 받아옵니다.
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(task, new CronTrigger(jobSchedule.getCronExpression()));
        // 예약된 작업을 맵에 저장하여 나중에 취소하거나 관리할 수 있도록 합니다.
        scheduledTasks.put(jobSchedule.getId(), scheduledFuture);
        log.info("Job {} (userId: {}) scheduled with cron: {}", jobSchedule.getId(), jobSchedule.getUserId(), jobSchedule.getCronExpression());
    }

    /**
     * @method cancelJob
     * @description 스케줄링된 작업을 취소합니다.
     *              맵에서 해당 `ScheduledFuture`를 찾아 `cancel(true)`를 호출하여 작업을 중단합니다.
     * @param jobScheduleId - 취소할 작업의 고유 ID
     */
    public void cancelJob(Long jobScheduleId) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(jobScheduleId);
        if (scheduledFuture != null) {
            // cancel(true): 현재 실행 중인 작업도 중단(interrupt)을 시도합니다.
            // cancel(false): 현재 실행 중인 작업은 완료될 때까지 기다리고, 다음 예약된 실행부터 취소합니다.
            scheduledFuture.cancel(true);
            scheduledTasks.remove(jobScheduleId);
            log.info("Job {} cancelled.", jobScheduleId);
        }
    }
}
