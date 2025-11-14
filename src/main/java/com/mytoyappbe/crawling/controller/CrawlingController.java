package com.mytoyappbe.crawling.controller;

import com.mytoyappbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 웹 크롤링 시작 요청을 처리하는 REST 컨트롤러입니다.
 * <p>
 * 클라이언트로부터 크롤링 시작 요청을 받아 비동기적으로 크롤링 프로세스를 트리거합니다.
 */
@RestController
@RequestMapping("/api/crawling")
@RequiredArgsConstructor
public class CrawlingController {

    /**
     * 크롤링 비즈니스 로직을 담당하는 서비스입니다.
     * Spring에 의해 주입됩니다.
     */
    private final CrawlingService crawlingService;

    /**
     * 현재 인증된 사용자에 대한 크롤링 프로세스를 시작하는 엔드포인트입니다.
     * <p>
     * {@code @PostMapping("/start")} 어노테이션은 HTTP POST 요청을 "/api/crawling/start" 경로로 매핑합니다.
     * {@code @AuthenticationPrincipal UserDetails userDetails}를 사용하여 현재 로그인한 사용자의 정보를 가져옵니다.
     * 이 메서드는 {@link CrawlingService}의 {@code startCrawling} 메서드를 호출하여 실제 크롤링 작업을 시작합니다.
     * 크롤링 작업은 비동기적으로 수행되며, 진행 상황은 WebSocket을 통해 클라이언트에 실시간으로 전달됩니다.
     *
     * @param userDetails 현재 인증된 사용자의 상세 정보
     */
    @PostMapping("/start") // userId를 PathVariable에서 제거
    public void startCrawling(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername(); // UserDetails에서 userId 가져오기
        crawlingService.startCrawling(userId);
    }
}
