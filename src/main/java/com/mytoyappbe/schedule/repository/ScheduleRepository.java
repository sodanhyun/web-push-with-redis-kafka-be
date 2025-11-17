/**
 * @file ScheduleRepository.java
 * @description {@link Schedule} 엔티티에 대한 데이터베이스 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 */
package com.mytoyappbe.schedule.repository;

import com.mytoyappbe.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * @interface ScheduleRepository
 * @description {@link Schedule} 엔티티에 대한 데이터베이스 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 *              `JpaRepository`를 상속받아 기본적인 CRUD(Create, Read, Update, Delete) 연산 및
 *              페이징, 정렬 기능을 자동으로 제공합니다.
 *              제네릭 타입으로 엔티티 클래스({@link Schedule})와 ID 타입({@link Long})을 지정합니다.
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * @method findByStatus
     * @description 주어진 상태({@link Schedule.JobStatus})를 가진 모든 크롤링 작업 스케줄을 조회합니다.
     *              Spring Data JPA의 쿼리 메서드 기능을 활용하여 메서드 이름만으로 쿼리를 자동으로 생성합니다.
     *              이 메서드는 주로 애플리케이션 시작 시 이전에 스케줄링되었던 작업들을 로드하여 다시 스케줄링하는 데 사용됩니다.
     * @param status - 조회할 작업의 상태 (예: {@link Schedule.JobStatus#SCHEDULED})
     * @return 해당 상태를 가진 {@link Schedule} 객체들의 리스트
     */
    List<Schedule> findByStatus(Schedule.JobStatus status);

    /**
     * @method findByUserId
     * @description 주어진 사용자 ID({@code userId})에 해당하는 모든 크롤링 작업 스케줄을 조회합니다.
     * @param userId - 조회할 사용자의 ID
     * @return 해당 사용자 ID를 가진 {@link Schedule} 객체들의 리스트
     */
    List<Schedule> findByUserId(String userId);

    /**
     * @method findByIdAndUserId
     * @description 주어진 ID({@code id})와 사용자 ID({@code userId})에 해당하는 크롤링 작업 스케줄을 조회합니다.
     *              이 메서드는 특정 스케줄이 특정 사용자의 소유인지 확인하는 데 사용됩니다.
     * @param id - 조회할 작업의 고유 ID
     * @param userId - 조회할 사용자의 ID
     * @return 해당 ID와 사용자 ID를 가진 {@link Schedule} 객체를 포함하는 {@link Optional}, 없으면 빈 {@link Optional}
     */
    Optional<Schedule> findByIdAndUserId(Long id, String userId);
}
