package com.mytoyappbe.schedule.service;

import com.mytoyappbe.schedule.entity.Schedule;
import com.mytoyappbe.schedule.repository.ScheduleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 크롤링 작업 스케줄을 관리하는 서비스 클래스입니다.
 * <p>
 * 데이터베이스에 스케줄 정보를 저장하고, {@link ScheduleManager}를 통해 실제 작업을 예약/취소/업데이트하며,
 * 애플리케이션 시작 시 기존 스케줄을 로드하여 재등록하는 역할을 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    /**
     * 크롤링 작업 스케줄 정보를 데이터베이스에 저장하고 조회하는 리포지토리입니다.
     */
    private final ScheduleRepository jobScheduleRepository;

    /**
     * 실제 크롤링 작업을 동적으로 스케줄링하고 취소하는 컴포넌트입니다.
     */
    private final ScheduleManager scheduleManager;

    /**
     * 애플리케이션 시작 시(빈 생성 및 의존성 주입 완료 후) 호출되는 메서드입니다.
     * <p>
     * 데이터베이스에 {@link Schedule.JobStatus#SCHEDULED} 상태로 저장된 모든 작업을 조회하여
     * {@link ScheduleManager}에 다시 등록합니다. 이는 애플리케이션이 재시작되더라도
     * 이전에 예약된 작업들이 유실되지 않고 계속 스케줄링되도록 보장합니다.
     */
    @PostConstruct
    public void initScheduledJobs() {
        List<Schedule> scheduledJobs = jobScheduleRepository.findByStatus(Schedule.JobStatus.SCHEDULED);
        log.info("Found {} SCHEDULED jobs to re-schedule on startup.", scheduledJobs.size());
        for (Schedule job : scheduledJobs) {
            scheduleManager.scheduleJob(job);
        }
    }

    /**
     * 새로운 크롤링 작업 스케줄을 추가합니다.
     * <p>
     * 작업 정보를 데이터베이스에 저장하고, 저장된 작업 정보를 기반으로 {@link ScheduleManager}에 작업을 예약합니다.
     * 이 메서드는 트랜잭션 내에서 실행되어 데이터의 일관성을 보장합니다.
     *
     * @param userId 스케줄링할 작업의 사용자 ID
     * @param cronExpression 작업을 실행할 Cron 표현식
     * @return 새로 추가된 {@link Schedule} 객체
     */
    @Transactional
    public Schedule addSchedule(String userId, String cronExpression) {
        Schedule jobSchedule = new Schedule();
        jobSchedule.setUserId(userId);
        jobSchedule.setCronExpression(cronExpression);
        jobSchedule.setJobName("crawlingJob"); // Spring Batch Job의 이름
        jobSchedule.setStatus(Schedule.JobStatus.SCHEDULED);
        Schedule savedJob = jobScheduleRepository.save(jobSchedule);
        scheduleManager.scheduleJob(savedJob);
        return savedJob;
    }

    /**
     * 지정된 ID의 크롤링 작업 스케줄을 취소합니다.
     * <p>
     * 데이터베이스에서 작업을 조회하고, {@link ScheduleManager}를 통해 예약된 작업을 취소한 후,
     * 데이터베이스에 작업 상태를 {@link Schedule.JobStatus#CANCELLED}로 업데이트합니다.
     * 이 메서드는 트랜잭션 내에서 실행됩니다.
     *
     * @param jobScheduleId 취소할 작업의 고유 ID
     * @return 취소된 {@link Schedule} 객체를 포함하는 {@link Optional}, 해당 ID의 작업이 없으면 빈 {@link Optional}
     */
    @Transactional
    public Optional<Schedule> cancelSchedule(Long jobScheduleId) {
        Optional<Schedule> optionalJob = jobScheduleRepository.findById(jobScheduleId);
        optionalJob.ifPresent(job -> {
            scheduleManager.cancelJob(job.getId()); // 스케줄러에서 작업 취소
            job.setStatus(Schedule.JobStatus.CANCELLED); // 데이터베이스 상태 업데이트
            jobScheduleRepository.save(job);
        });
        return optionalJob;
    }

    /**
     * 지정된 ID의 크롤링 작업 스케줄의 Cron 표현식을 업데이트합니다.
     * <p>
     * 기존 작업을 취소하고, 새로운 Cron 표현식으로 업데이트한 후 데이터베이스에 저장하고
     * {@link ScheduleManager}에 새로운 Cron 표현식으로 작업을 재예약합니다.
     * 이 메서드는 트랜잭_ 내에서 실행됩니다.
     *
     * @param jobScheduleId 업데이트할 작업의 고유 ID
     * @param newCronExpression 새로 설정할 Cron 표현식
     * @return 업데이트된 {@link Schedule} 객체를 포함하는 {@link Optional}, 해당 ID의 작업이 없으면 빈 {@link Optional}
     */
    @Transactional
    public Optional<Schedule> updateSchedule(Long jobScheduleId, String newCronExpression) {
        Optional<Schedule> optionalJob = jobScheduleRepository.findById(jobScheduleId);
        optionalJob.ifPresent(job -> {
            scheduleManager.cancelJob(job.getId()); // 기존 스케줄러 작업 취소
            job.setCronExpression(newCronExpression);
            job.setStatus(Schedule.JobStatus.SCHEDULED); // 상태 재설정
            Schedule updatedJob = jobScheduleRepository.save(job);
            scheduleManager.scheduleJob(updatedJob); // 새 Cron으로 재스케줄링
        });
        return optionalJob;
    }

    /**
     * 모든 크롤링 작업 스케줄 목록을 조회합니다.
     *
     * @return 모든 {@link Schedule} 객체들의 리스트
     */
    public List<Schedule> getAllSchedules() {
        return jobScheduleRepository.findAll();
    }

    /**
     * 지정된 ID의 크롤링 작업 스케줄을 조회합니다.
     *
     * @param jobScheduleId 조회할 작업의 고유 ID
     * @return 해당 ID의 {@link Schedule} 객체를 포함하는 {@link Optional}, 없으면 빈 {@link Optional}
     */
    public Optional<Schedule> getScheduleById(Long jobScheduleId) {
        return jobScheduleRepository.findById(jobScheduleId);
    }
}
