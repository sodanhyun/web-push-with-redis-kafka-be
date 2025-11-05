package com.mytoyappbe.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 동적으로 스케줄링된 크롤링 작업의 정보를 저장하는 JPA 엔티티 클래스입니다.
 * <p>
 * 이 엔티티는 데이터베이스의 `crawling_job_schedule` 테이블과 매핑되며,
 * 각 스케줄링된 작업의 고유 ID, 사용자 ID, Cron 표현식, 작업 이름, 파라미터, 상태 및 타임스탬프를 관리합니다.
 */
@Entity
@Table(name = "crawling_job_schedule") // 데이터베이스 테이블 이름을 지정합니다.
@Data // Lombok 어노테이션: Getter, Setter, toString, equals, hashCode를 자동으로 생성합니다.
public class CrawlingJobSchedule {

    /**
     * 스케줄링된 작업의 고유 식별자 (Primary Key).
     * 데이터베이스에서 자동으로 생성되는 값을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL의 AUTO_INCREMENT와 같은 전략을 사용합니다.
    private Long id;

    /**
     * 크롤링 작업을 요청한 사용자의 ID입니다.
     * 이 필드는 null을 허용하지 않습니다.
     */
    @Column(nullable = false)
    private String userId;

    /**
     * 작업을 스케줄링할 Cron 표현식입니다.
     * (예: "0 0 12 * * ?"는 매일 정오에 실행).
     * 이 필드는 null을 허용하지 않습니다.
     */
    @Column(nullable = false)
    private String cronExpression;

    /**
     * 실행될 Spring Batch Job의 이름입니다.
     * (예: "crawlingJob"). 이 필드는 null을 허용하지 않습니다.
     */
    @Column(nullable = false)
    private String jobName;

    /**
     * Spring Batch Job에 전달될 추가 파라미터입니다.
     * JSON 문자열 형태로 저장될 수 있으며, 필요에 따라 파싱하여 사용합니다.
     * `columnDefinition = "TEXT"`를 통해 긴 문자열 저장을 허용합니다.
     */
    @Column(columnDefinition = "TEXT")
    private String jobParameters;

    /**
     * 현재 스케줄링된 작업의 상태입니다.
     * {@link JobStatus} enum을 사용하여 상태를 관리하며, 데이터베이스에는 문자열 형태로 저장됩니다.
     * 이 필드는 null을 허용하지 않습니다.
     */
    @Enumerated(EnumType.STRING) // Enum 값을 데이터베이스에 문자열로 저장합니다.
    @Column(nullable = false)
    private JobStatus status;

    /**
     * 레코드가 생성된 시각입니다.
     * 엔티티가 처음 저장될 때 자동으로 타임스탬프가 기록되며, 이후 업데이트되지 않습니다.
     */
    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시각을 기록합니다.
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 레코드가 마지막으로 업데이트된 시각입니다.
     * 엔티티가 업데이트될 때마다 자동으로 타임스탬프가 갱신됩니다.
     */
    @UpdateTimestamp // 엔티티 업데이트 시 자동으로 현재 시각을 갱신합니다.
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 스케줄링된 작업의 가능한 상태를 정의하는 Enum입니다.
     */
    public enum JobStatus {
        /** 작업이 스케줄링되었고 실행 대기 중입니다. */
        SCHEDULED,
        /** 작업이 현재 실행 중입니다. */
        RUNNING,
        /** 작업이 성공적으로 완료되었습니다. */
        COMPLETED,
        /** 작업이 수동으로 취소되었습니다. */
        CANCELLED,
        /** 작업 실행 중 오류가 발생하여 실패했습니다. */
        FAILED
    }
}
