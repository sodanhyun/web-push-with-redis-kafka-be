//package com.mytoyappbe.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "crawling_job_schedule")
//@Data
//public class CrawlingJobSchedule {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String userId;
//
//    @Column(nullable = false)
//    private String cronExpression;
//
//    @Column(nullable = false)
//    private String jobName;
//
//    @Column(columnDefinition = "TEXT")
//    private String jobParameters;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private JobStatus status;
//
//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(nullable = false)
//    private LocalDateTime updatedAt;
//
//    public enum JobStatus {
//        SCHEDULED,
//        RUNNING,
//        COMPLETED,
//        CANCELLED,
//        FAILED
//    }
//}
