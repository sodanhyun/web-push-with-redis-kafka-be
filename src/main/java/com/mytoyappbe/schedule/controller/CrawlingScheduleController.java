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
 * 크롤링 작업 스케줄을 관리하기 위한 REST API 컨트롤러입니다.
 * <p>
 * 이 컨트롤러는 크롤링 작업 스케줄을 추가, 조회, 취소, 업데이트하는 엔드포인트를 제공합니다.
 * 모든 요청은 {@link ScheduleService}를 통해 처리됩니다.
 */
@RestController
@RequestMapping("/api/schedules/crawling")
@RequiredArgsConstructor
public class CrawlingScheduleController {

    /**
     * 크롤링 작업 스케줄의 비즈니스 로직을 처리하는 서비스입니다.
     * Spring에 의해 주입됩니다.
     */
    private final ScheduleService scheduleService;

    /**
     * 새로운 크롤링 작업 스케줄을 추가합니다.
     * <p>
     * 요청 본문에서 {@code cronExpression}을 받아 새로운 스케줄을 등록합니다.
     * {@code @AuthenticationPrincipal UserDetails userDetails}를 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * 성공 시 HTTP 201 Created 상태 코드와 함께 새로 생성된 스케줄 정보를 반환합니다.
     * 필수 파라미터가 누락된 경우 HTTP 400 Bad Request를 반환합니다.
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param payload 요청 본문에 포함된 JSON 데이터 (cronExpression)
     * @return 새로 생성된 {@link Schedule} 객체와 HTTP 상태 코드
     */
    @PostMapping
    public ResponseEntity<Schedule> addCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> payload) {
        String userId = userDetails.getUsername();
        String cronExpression = payload.get("cronExpression");

        if (cronExpression == null) {
            return ResponseEntity.badRequest().build(); // 필수 파라미터 누락 시 400 Bad Request
        }

        Schedule newSchedule = scheduleService.addSchedule(userId, cronExpression);
        return new ResponseEntity<>(newSchedule, HttpStatus.CREATED); // 201 Created
    }

    /**
     * 현재 인증된 사용자의 모든 크롤링 작업 스케줄 목록을 조회합니다.
     * <p>
     * {@code @AuthenticationPrincipal UserDetails userDetails}를 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * 성공 시 HTTP 200 OK 상태 코드와 함께 해당 사용자의 스케줄 목록을 반환합니다.
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 해당 사용자의 {@link Schedule} 객체들의 리스트와 HTTP 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> getMyCrawlingSchedules(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        List<Schedule> schedules = scheduleService.getSchedulesByUserId(userId); // 새로운 서비스 메서드 호출
        return ResponseEntity.ok(schedules); // 200 OK
    }

    /**
     * 지정된 ID의 크롤링 작업 스케줄을 취소합니다.
     * <p>
     * {@code @AuthenticationPrincipal UserDetails userDetails}를 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * 성공적으로 취소되면 HTTP 204 No Content 상태 코드를 반환합니다.
     * 해당 ID의 스케줄이 존재하지 않거나, 현재 사용자의 스케줄이 아니면 HTTP 404 Not Found를 반환합니다.
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param id 취소할 크롤링 작업 스케줄의 고유 ID
     * @return HTTP 상태 코드 (204 No Content 또는 404 Not Found)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        String userId = userDetails.getUsername();
        return scheduleService.cancelSchedule(id, userId) // 서비스 메서드에 userId 추가
                .map(job -> ResponseEntity.noContent().<Void>build()) // 204 No Content
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    /**
     * 지정된 ID의 크롤링 작업 스케줄의 Cron 표현식을 업데이트합니다.
     * <p>
     * 요청 본문에서 새로운 {@code cronExpression}을 받아 해당 스케줄을 업데이트합니다.
     * {@code @AuthenticationPrincipal UserDetails userDetails}를 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * 성공 시 HTTP 200 OK 상태 코드와 함께 업데이트된 스케줄 정보를 반환합니다.
     * 필수 파라미터가 누락된 경우 HTTP 400 Bad Request를 반환합니다.
     * 해당 ID의 스케줄이 존재하지 않거나, 현재 사용자의 스케줄이 아니면 HTTP 404 Not Found를 반환합니다.
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @param id 업데이트할 크롤링 작업 스케줄의 고유 ID
     * @param payload 요청 본문에 포함된 JSON 데이터 (cronExpression)
     * @return 업데이트된 {@link Schedule} 객체와 HTTP 상태 코드
     */
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateCrawlingSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String userId = userDetails.getUsername();
        String newCronExpression = payload.get("cronExpression");

        if (newCronExpression == null) {
            return ResponseEntity.badRequest().build(); // 필수 파라미터 누락 시 400 Bad Request
        }

        return scheduleService.updateSchedule(id, userId, newCronExpression) // 서비스 메서드에 userId 추가
                .map(ResponseEntity::ok) // 200 OK
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }
}
