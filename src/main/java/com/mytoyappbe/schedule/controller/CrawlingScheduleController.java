/**
 * @file CrawlingScheduleController.java
 * @description 크롤링 작업 스케줄을 관리하기 위한 REST API 컨트롤러입니다.
 *              크롤링 작업 스케줄을 추가, 조회, 취소, 업데이트하는 엔드포인트를 제공하며,
 *              모든 요청은 {@link ScheduleService}를 통해 처리됩니다.
 */

package com.mytoyappbe.schedule.controller;

import com.mytoyappbe.schedule.entity.Schedule;
import com.mytoyappbe.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @class CrawlingScheduleController
 * @description 크롤링 작업 스케줄을 관리하기 위한 REST API를 제공하는 컨트롤러입니다.
 *              `/api/schedules/crawling` 경로로 들어오는 요청을 매핑합니다.
 */
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/schedules/crawling") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/schedules/crawling" 경로를 기본으로 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class CrawlingScheduleController {

    private final ScheduleService scheduleService; // 크롤링 작업 스케줄의 비즈니스 로직을 처리하는 서비스

    /**
     * @method addCrawlingSchedule
     * @description 새로운 크롤링 작업 스케줄을 추가합니다.
     *              요청 본문에서 `cronExpression`을 받아 새로운 스케줄을 등록합니다.
     *              `@AuthenticationPrincipal`을 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보
     * @param {Map<String, String>} payload - 요청 본문에 포함된 JSON 데이터 (cronExpression 필드 포함)
     * @returns {ResponseEntity<Schedule>} 새로 생성된 {@link Schedule} 객체와 HTTP 상태 코드 (201 Created 또는 400 Bad Request)
     */
    @PostMapping // HTTP POST 요청을 "/api/schedules/crawling" 경로에 매핑합니다.
    public ResponseEntity<Schedule> addCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {
        String userId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID를 가져옵니다.
        String cronExpression = payload.get("cronExpression"); // 요청 본문에서 cronExpression을 추출합니다.

        if (cronExpression == null) {
            return ResponseEntity.badRequest().build(); // 필수 파라미터 누락 시 400 Bad Request 응답
        }

        Schedule newSchedule = scheduleService.addSchedule(userId, cronExpression); // ScheduleService를 통해 스케줄 추가
        return new ResponseEntity<>(newSchedule, HttpStatus.CREATED); // 201 Created 응답
    }

    /**
     * @method getMyCrawlingSchedules
     * @description 현재 인증된 사용자의 모든 크롤링 작업 스케줄 목록을 조회합니다.
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보
     * @returns {ResponseEntity<List<Schedule>>} 해당 사용자의 {@link Schedule} 객체들의 리스트와 HTTP 상태 코드 (200 OK)
     */
    @GetMapping // HTTP GET 요청을 "/api/schedules/crawling" 경로에 매핑합니다.
    public ResponseEntity<List<Schedule>> getMyCrawlingSchedules(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID를 가져옵니다.
        List<Schedule> schedules = scheduleService.getSchedulesByUserId(userId); // ScheduleService를 통해 스케줄 목록 조회
        return ResponseEntity.ok(schedules); // 200 OK 응답
    }

    /**
     * @method cancelCrawlingSchedule
     * @description 지정된 ID의 크롤링 작업 스케줄을 취소합니다.
     *              해당 스케줄이 현재 인증된 사용자의 소유인지 확인합니다.
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보
     * @param {Long} id - 취소할 크롤링 작업 스케줄의 고유 ID
     * @returns {ResponseEntity<Void>} HTTP 상태 코드 (204 No Content 또는 404 Not Found)
     */
    @DeleteMapping("/{id}") // HTTP DELETE 요청을 "/api/schedules/crawling/{id}" 경로에 매핑합니다.
    public ResponseEntity<Void> cancelCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String userId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID를 가져옵니다.
        return scheduleService.cancelSchedule(id, userId) // ScheduleService를 통해 스케줄 취소
                .map(job -> ResponseEntity.noContent().<Void>build()) // 취소 성공 시 204 No Content
                .orElse(ResponseEntity.notFound().build()); // 스케줄을 찾을 수 없거나 소유자가 다르면 404 Not Found
    }

    /**
     * @method updateCrawlingSchedule
     * @description 지정된 ID의 크롤링 작업 스케줄의 Cron 표현식을 업데이트합니다.
     *              해당 스케줄이 현재 인증된 사용자의 소유인지 확인합니다.
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보
     * @param {Long} id - 업데이트할 크롤링 작업 스케줄의 고유 ID
     * @param {Map<String, String>} payload - 요청 본문에 포함된 JSON 데이터 (newCronExpression 필드 포함)
     * @returns {ResponseEntity<Schedule>} 업데이트된 {@link Schedule} 객체와 HTTP 상태 코드 (200 OK, 400 Bad Request 또는 404 Not Found)
     */
    @PutMapping("/{id}") // HTTP PUT 요청을 "/api/schedules/crawling/{id}" 경로에 매핑합니다.
    public ResponseEntity<Schedule> updateCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String userId = userDetails.getUsername(); // 현재 로그인한 사용자의 ID를 가져옵니다.
        String newCronExpression = payload.get("cronExpression"); // 요청 본문에서 새로운 cronExpression을 추출합니다.

        if (newCronExpression == null) {
            return ResponseEntity.badRequest().build(); // 필수 파라미터 누락 시 400 Bad Request 응답
        }

        return scheduleService.updateSchedule(id, userId, newCronExpression) // ScheduleService를 통해 스케줄 업데이트
                .map(ResponseEntity::ok) // 업데이트 성공 시 200 OK
                .orElse(ResponseEntity.notFound().build()); // 스케줄을 찾을 수 없거나 소유자가 다르면 404 Not Found
    }
}