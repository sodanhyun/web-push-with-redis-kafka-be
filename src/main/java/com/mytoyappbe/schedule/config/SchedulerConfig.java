package com.mytoyappbe.schedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * {@link ThreadPoolTaskScheduler} 빈을 설정하고 초기화하는 내부 정적 설정 클래스입니다.
 * <p>
 * 이 클래스는 {@link com.mytoyappbe.schedule.service.ScheduleManager}가 의존하는 {@link ThreadPoolTaskScheduler}를 Spring 컨텍스트에 등록합니다.
 */
@Component
public class SchedulerConfig {
    /**
     * 동적 스케줄링을 위한 {@link ThreadPoolTaskScheduler} 빈을 생성합니다.
     * <p>
     * 스레드 풀 크기, 스레드 이름 접두사 등을 설정하여 스케줄러의 동작을 정의합니다。
     *
     * @return 설정 및 초기화된 {@link ThreadPoolTaskScheduler} 인스턴스
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 스케줄러가 사용할 스레드 풀의 크기를 설정합니다. 동시 실행될 작업의 최대 개수를 의미합니다.
        scheduler.setThreadNamePrefix("dynamic-scheduler-"); // 스케줄러 스레드의 이름 접두사를 설정합니다.
        scheduler.initialize(); // 스케줄러를 초기화하고 스레드 풀을 시작합니다.
        return scheduler;
    }
}