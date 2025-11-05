package com.mytoyappbe.repository;

import com.mytoyappbe.entity.CrawlingJobSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link CrawlingJobSchedule} 엔티티에 대한 데이터베이스 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 * <p>
 * {@link JpaRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete) 연산 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * 제네릭 타입으로 엔티티 클래스({@link CrawlingJobSchedule})와 ID 타입({@link Long})을 지정합니다.
 */
@Repository
public interface CrawlingJobScheduleRepository extends JpaRepository<CrawlingJobSchedule, Long> {

    /**
     * 주어진 상태({@link CrawlingJobSchedule.JobStatus})를 가진 모든 크롤링 작업 스케줄을 조회합니다.
     * <p>
     * Spring Data JPA의 쿼리 메서드 기능을 활용하여 메서드 이름만으로 쿼리를 자동으로 생성합니다.
     * 이 메서드는 주로 애플리케이션 시작 시 이전에 스케줄링되었던 작업들을 로드하여 다시 스케줄링하는 데 사용됩니다.
     *
     * @param status 조회할 작업의 상태 (예: {@link CrawlingJobSchedule.JobStatus#SCHEDULED})
     * @return 해당 상태를 가진 {@link CrawlingJobSchedule} 객체들의 리스트
     */
    List<CrawlingJobSchedule> findByStatus(CrawlingJobSchedule.JobStatus status);
}
