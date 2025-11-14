/**
 * @file Schedule.java
 * @description 동적으로 스케줄링된 크롤링 작업의 정보를 저장하는 JPA 엔티티 클래스입니다.
 */

package com.mytoyappbe.schedule.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * @class Schedule
 * @description 동적으로 스케줄링된 크롤링 작업의 정보를 데이터베이스에 매핑하는 JPA 엔티티입니다.
 *              `crawling_job_schedule` 테이블과 매핑되며, 작업의 ID, 사용자 ID, Cron 표현식,
 *              작업 이름, 파라미터, 상태, 생성 및 업데이트 시간을 관리합니다.
 */
@Entity // 이 클래스가 JPA 엔티티임을 나타냅니다.
@Table(name = "crawling_job_schedule") // 데이터베이스 테이블 이름을 "crawling_job_schedule"로 지정합니다.
@Data // Lombok 어노테이션: getter, setter, toString, equals, hashCode 메서드를 자동으로 생성합니다.
public class Schedule {

    @Id // 기본 키(Primary Key)임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 데이터베이스에 위임합니다.
    private Long id; // 스케줄링된 작업의 고유 식별자

    @Column(nullable = false) // null을 허용하지 않습니다.
    private String userId; // 크롤링 작업을 요청한 사용자의 ID

    @Column(nullable = false) // null을 허용하지 않습니다.
    private String cronExpression; // 작업을 스케줄링할 Cron 표현식 (예: "0 0 12 * * ?")

    @Column(nullable = false) // null을 허용하지 않습니다.
    private String jobName; // 실행될 Spring Batch Job의 이름 (예: "crawlingJob")

    @Column(columnDefinition = "TEXT") // 긴 문자열 저장을 위해 TEXT 타입으로 지정합니다.
    private String jobParameters; // Spring Batch Job에 전달될 추가 파라미터 (JSON 문자열 형태)

    @Enumerated(EnumType.STRING) // Enum 값을 데이터베이스에 문자열 형태로 저장합니다.
    @Column(nullable = false) // null을 허용하지 않습니다.
    private JobStatus status; // 현재 스케줄링된 작업의 상태

    @CreationTimestamp // 엔티티가 처음 저장될 때 자동으로 현재 시각을 기록합니다.
    @Column(nullable = false, updatable = false) // null을 허용하지 않으며, 업데이트되지 않습니다.
    private LocalDateTime createdAt; // 레코드가 생성된 시각

    @UpdateTimestamp // 엔티티가 업데이트될 때마다 자동으로 현재 시각을 갱신합니다.
    @Column(nullable = false) // null을 허용하지 않습니다.
    private LocalDateTime updatedAt; // 레코드가 마지막으로 업데이트된 시각

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