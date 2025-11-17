/**
 * @file CrawlingScheduleController.java
 * @description 크롤링 작업 스케줄을 관리하기 위한 REST API 컨트롤러입니다.
 *              크롤링 작업 스케줄을 추가, 조회, 취소, 업데이트하는 엔드포인트를 제공하며,
 *              모든 요청은 {@link ScheduleService}를 통해 처리됩니다.
 */
package com.mytoyappbe.schedule.controller;

import com.mytoyappbe.schedule.dto.CrawlingScheduleDto;
import com.mytoyappbe.schedule.entity.Schedule;
import com.mytoyappbe.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @class CrawlingScheduleController
 * @description 크롤링 작업 스케줄을 관리하기 위한 REST API를 제공하는 컨트롤러입니다.
 *              `/api/schedules/crawling` 경로로 들어오는 요청을 매핑합니다.
 */
@RestController
@RequestMapping("/api/schedules/crawling")
@RequiredArgsConstructor
public class CrawlingScheduleController {

    private final ScheduleService scheduleService;

    /**
     * @method addCrawlingSchedule
     * @description 새로운 크롤링 작업 스케줄을 추가합니다.
     *              요청 본문에서 `cronExpression`을 받아 새로운 스케줄을 등록합니다.
     *              `@AuthenticationPrincipal`을 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * @param userDetails - 현재 인증된 사용자의 상세 정보
     * @param scheduleDto - 요청 본문에 포함된 스케줄 데이터 (cronExpression 필드 포함)
     * @return 새로 생성된 {@link Schedule} 객체와 HTTP 상태 코드 (201 Created 또는 400 Bad Request)
     */
    @PostMapping
    public ResponseEntity<Schedule> addCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CrawlingScheduleDto scheduleDto) {
        String userId = userDetails.getUsername();
        String cronExpression = scheduleDto.getCronExpression();

        if (cronExpression == null || cronExpression.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Schedule newSchedule = scheduleService.addSchedule(userId, cronExpression);
        return new ResponseEntity<>(newSchedule, HttpStatus.CREATED);
    }

    /**
     * @method getMyCrawlingSchedules
     * @description 현재 인증된 사용자의 모든 크롤링 작업 스케줄 목록을 조회합니다.
     * @param userDetails - 현재 인증된 사용자의 상세 정보
     * @return 해당 사용자의 {@link Schedule} 객체들의 리스트와 HTTP 상태 코드 (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> getMyCrawlingSchedules(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        List<Schedule> schedules = scheduleService.getSchedulesByUserId(userId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * @method cancelCrawlingSchedule
     * @description 지정된 ID의 크롤링 작업 스케줄을 취소합니다.
     *              해당 스케줄이 현재 인증된 사용자의 소유인지 확인합니다.
     * @param userDetails - 현재 인증된 사용자의 상세 정보
     * @param id          - 취소할 크롤링 작업 스케줄의 고유 ID
     * @return HTTP 상태 코드 (204 No Content 또는 404 Not Found)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String userId = userDetails.getUsername();
        return scheduleService.cancelSchedule(id, userId)
                .map(job -> ResponseEntity.noContent().<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @method updateCrawlingSchedule
     * @description 지정된 ID의 크롤링 작업 스케줄의 Cron 표현식을 업데이트합니다.
     *              해당 스케줄이 현재 인증된 사용자의 소유인지 확인합니다.
     * @param userDetails - 현재 인증된 사용자의 상세 정보
     * @param id          - 업데이트할 크롤링 작업 스케줄의 고유 ID
     * @param scheduleDto - 요청 본문에 포함된 스케줄 데이터 (cronExpression 필드 포함)
     * @return 업데이트된 {@link Schedule} 객체와 HTTP 상태 코드 (200 OK, 400 Bad Request 또는 404 Not Found)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody CrawlingScheduleDto scheduleDto) {
        String userId = userDetails.getUsername();
        String newCronExpression = scheduleDto.getCronExpression();

        if (newCronExpression == null || newCronExpression.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return scheduleService.updateSchedule(id, userId, newCronExpression)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}