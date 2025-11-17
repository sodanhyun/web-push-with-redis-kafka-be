/**
 * @file Schedule.java
 * @description 동적으로 스케줄링된 크롤링 작업의 정보를 저장하는 JPA 엔티티 클래스입니다.
 */
package com.mytoyappbe.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * @class Schedule
 * @description 동적으로 스케줄링된 크롤링 작업의 정보를 데이터베이스에 매핑하는 JPA 엔티티입니다.
 *              `crawling_job_schedule` 테이블과 매핑되며, 작업의 ID, 사용자 ID, Cron 표현식,
 *              작업 이름, 파라미터, 상태, 생성 및 업데이트 시간을 관리합니다.
 */
@Entity
@Table(name = "crawling_job_schedule")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String cronExpression;

    @Column(nullable = false)
    private String jobName;

    @Column(columnDefinition = "TEXT")
    private String jobParameters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * @enum JobStatus
     * @description 스케줄링된 작업의 가능한 상태를 정의하는 열거형(Enum)입니다.
     */
    public enum JobStatus {
        SCHEDULED, // 작업이 스케줄링되었고 실행 대기 중입니다.
        RUNNING,   // 작업이 현재 실행 중입니다.
        COMPLETED, // 작업이 성공적으로 완료되었습니다.
        CANCELLED, // 작업이 수동으로 취소되었습니다.
        FAILED     // 작업 실행 중 오류가 발생하여 실패했습니다.
    }
}