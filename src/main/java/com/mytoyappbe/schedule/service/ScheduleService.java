/**
 * @file ScheduleService.java
 * @description 크롤링 작업 스케줄을 관리하는 서비스 클래스입니다.
 *              데이터베이스에 스케줄 정보를 저장하고, {@link ScheduleManager}를 통해 실제 작업을 예약/취소/업데이트하며,
 *              애플리케이션 시작 시 기존 스케줄을 로드하여 재등록하는 역할을 수행합니다.
 */

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
 * @class ScheduleService
 * @description 크롤링 작업 스케줄의 생성, 조회, 수정, 취소와 같은 비즈니스 로직을 처리하는 서비스입니다.
 *              데이터베이스와 `ScheduleManager`를 연동하여 스케줄을 관리합니다.
 */
@Slf4j // 로깅을 위한 Lombok 어노테이션
@Service // Spring 서비스 컴포넌트임을 나타냅니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션으로 설정
public class ScheduleService {

    private final ScheduleRepository jobScheduleRepository; // 크롤링 작업 스케줄 정보를 데이터베이스에 저장하고 조회하는 리포지토리
    private final ScheduleManager scheduleManager; // 실제 크롤링 작업을 동적으로 스케줄링하고 취소하는 컴포넌트

    /**
     * @method initScheduledJobs
     * @description 애플리케이션 시작 시(빈 생성 및 의존성 주입 완료 후) 호출되는 메서드입니다.
     *              데이터베이스에 `SCHEDULED` 상태로 저장된 모든 작업을 조회하여
     *              `ScheduleManager`에 다시 등록합니다. 이는 애플리케이션이 재시작되더라도
     *              이전에 예약된 작업들이 유실되지 않고 계속 스케줄링되도록 보장합니다.
     */
    @PostConstruct // 빈 생성 및 의존성 주입 완료 후 이 메서드를 한 번 실행합니다.
    public void initScheduledJobs() {
        List<Schedule> scheduledJobs = jobScheduleRepository.findByStatus(Schedule.JobStatus.SCHEDULED);
        log.info("Found {} SCHEDULED jobs to re-schedule on startup.", scheduledJobs.size());
        for (Schedule job : scheduledJobs) {
            scheduleManager.scheduleJob(job);
        }
    }

    /**
     * @method addSchedule
     * @description 새로운 크롤링 작업 스케줄을 추가합니다.
     *              작업 정보를 데이터베이스에 저장하고, 저장된 작업 정보를 기반으로 `ScheduleManager`에 작업을 예약합니다.
     *              이 메서드는 트랜잭션 내에서 실행되어 데이터의 일관성을 보장합니다.
     * @param {String} userId - 스케줄링할 작업의 사용자 ID
     * @param {String} cronExpression - 작업을 실행할 Cron 표현식
     * @returns {Schedule} 새로 추가된 {@link Schedule} 객체
     */
    @Transactional // 메서드 실행 중 데이터베이스 작업에 트랜잭션을 적용합니다.
    public Schedule addSchedule(String userId, String cronExpression) {
        Schedule jobSchedule = new Schedule();
        jobSchedule.setUserId(userId);
        jobSchedule.setCronExpression(cronExpression);
        jobSchedule.setJobName("crawlingJob"); // Spring Batch Job의 이름
        jobSchedule.setStatus(Schedule.JobStatus.SCHEDULED); // 초기 상태는 SCHEDULED
        Schedule savedJob = jobScheduleRepository.save(jobSchedule); // 데이터베이스에 저장
        scheduleManager.scheduleJob(savedJob); // ScheduleManager에 작업 예약
        return savedJob;
    }

    /**
     * @method getSchedulesByUserId
     * @description 특정 사용자의 모든 크롤링 작업 스케줄 목록을 조회합니다.
     * @param {String} userId - 스케줄을 조회할 사용자의 ID
     * @returns {List<Schedule>} 해당 사용자의 {@link Schedule} 객체들의 리스트
     */
    public List<Schedule> getSchedulesByUserId(String userId) {
        return jobScheduleRepository.findByUserId(userId);
    }

    /**
     * @method cancelSchedule
     * @description 지정된 ID의 크롤링 작업 스케줄을 취소합니다.
     *              데이터베이스에서 작업을 조회하고, `ScheduleManager`를 통해 예약된 작업을 취소한 후,
     *              데이터베이스에 작업 상태를 `CANCELLED`로 업데이트합니다.
     * @param {Long} jobScheduleId - 취소할 작업의 고유 ID
     * @param {String} userId - 요청한 사용자의 ID (소유권 확인용)
     * @returns {Optional<Schedule>} 취소된 {@link Schedule} 객체를 포함하는 {@link Optional}, 해당 ID의 작업이 없거나 소유자가 다르면 빈 {@link Optional}
     */
    @Transactional // 메서드 실행 중 데이터베이스 작업에 트랜잭션을 적용합니다.
    public Optional<Schedule> cancelSchedule(Long jobScheduleId, String userId) {
        // ID와 userId를 사용하여 작업의 소유권을 확인하며 조회합니다.
        Optional<Schedule> optionalJob = jobScheduleRepository.findByIdAndUserId(jobScheduleId, userId);
        optionalJob.ifPresent(job -> {
            scheduleManager.cancelJob(job.getId()); // 스케줄러에서 작업 취소
            job.setStatus(Schedule.JobStatus.CANCELLED); // 데이터베이스 상태를 CANCELLED로 업데이트
            jobScheduleRepository.save(job); // 변경된 상태를 데이터베이스에 저장
        });
        return optionalJob;
    }

    /**
     * @method updateSchedule
     * @description 지정된 ID의 크롤링 작업 스케줄의 Cron 표현식을 업데이트합니다.
     *              기존 작업을 취소하고, 새로운 Cron 표현식으로 업데이트한 후 데이터베이스에 저장하고
     *              `ScheduleManager`에 새로운 Cron 표현식으로 작업을 재예약합니다.
     * @param {Long} jobScheduleId - 업데이트할 작업의 고유 ID
     * @param {String} userId - 요청한 사용자의 ID (소유권 확인용)
     * @param {String} newCronExpression - 새로 설정할 Cron 표현식
     * @returns {Optional<Schedule>} 업데이트된 {@link Schedule} 객체를 포함하는 {@link Optional}, 해당 ID의 작업이 없거나 소유자가 다르면 빈 {@link Optional}
     */
    @Transactional // 메서드 실행 중 데이터베이스 작업에 트랜잭션을 적용합니다.
    public Optional<Schedule> updateSchedule(Long jobScheduleId, String userId, String newCronExpression) {
        // ID와 userId를 사용하여 작업의 소유권을 확인하며 조회합니다.
        Optional<Schedule> optionalJob = jobScheduleRepository.findByIdAndUserId(jobScheduleId, userId);
        optionalJob.ifPresent(job -> {
            scheduleManager.cancelJob(job.getId()); // 기존 스케줄러 작업 취소
            job.setCronExpression(newCronExpression); // Cron 표현식 업데이트
            job.setStatus(Schedule.JobStatus.SCHEDULED); // 상태를 SCHEDULED로 재설정
            Schedule updatedJob = jobScheduleRepository.save(job); // 변경된 스케줄을 데이터베이스에 저장
            scheduleManager.scheduleJob(updatedJob); // ScheduleManager에 새 Cron 표현식으로 작업 재예약
        });
        return optionalJob;
    }

    /**
     * @method getScheduleById
     * @description 지정된 ID의 크롤링 작업 스케줄을 조회합니다.
     * @param {Long} jobScheduleId - 조회할 작업의 고유 ID
     * @returns {Optional<Schedule>} 해당 ID의 {@link Schedule} 객체를 포함하는 {@link Optional}, 없으면 빈 {@link Optional}
     */
    public Optional<Schedule> getScheduleById(Long jobScheduleId) {
        return jobScheduleRepository.findById(jobScheduleId);
    }
}