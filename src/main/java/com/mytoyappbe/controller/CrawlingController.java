package com.mytoyappbe.controller;

import com.mytoyappbe.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 웹 크롤링 시작 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 크롤링 시작 요청을 받아 비동기적으로 크롤링 프로세스를 트리거합니다.
 */
@RestController
@RequestMapping("/api/crawling") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/crawling" 경로를 기본으로 합니다.
@RequiredArgsConstructor
public class CrawlingController {

    /**
     * 크롤링 비즈니스 로직을 담당하는 서비스입니다.
     * Spring에 의해 주입됩니다.
     */
    private final CrawlingService crawlingService;

    /**
     * 특정 사용자 ID에 대한 크롤링 프로세스를 시작하는 엔드포인트입니다.
     * <p>
     * {@code @PostMapping("/start/{userId}")} 어노테이션은 HTTP POST 요청을 "/api/crawling/start/{userId}" 경로로 매핑합니다.
     * {@code @PathVariable String userId}는 URL 경로에서 {@code userId} 값을 추출하여 메서드 파라미터로 전달합니다.
     * 이 메서드는 {@link CrawlingService}의 {@code startCrawling} 메서드를 호출하여 실제 크롤링 작업을 시작합니다.
     * 크롤링 작업은 비동기적으로 수행되며, 진행 상황은 WebSocket을 통해 클라이언트에 실시간으로 전달됩니다.
     *
     * @param userId 크롤링을 시작할 사용자의 ID
     */
    @PostMapping("/start/{userId}")
    public void startCrawling(@PathVariable String userId) {
        crawlingService.startCrawling(userId);
    }
}
