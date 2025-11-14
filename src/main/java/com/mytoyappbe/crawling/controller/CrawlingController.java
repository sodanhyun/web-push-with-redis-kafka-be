/**
 * @file CrawlingController.java
 * @description 웹 크롤링 시작 요청을 처리하는 REST 컨트롤러입니다.
 *              클라이언트로부터 크롤링 시작 요청을 받아 비동기적으로 크롤링 프로세스를 트리거합니다.
 */

package com.mytoyappbe.crawling.controller;

import com.mytoyappbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class CrawlingController
 * @description 웹 크롤링 시작 요청을 처리하는 REST 컨트롤러입니다.
 *              `/api/crawling` 경로로 들어오는 요청을 매핑합니다.
 */
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/crawling") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/crawling" 경로를 기본으로 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class CrawlingController {

    private final CrawlingService crawlingService; // 크롤링 관련 비즈니스 로직을 처리하는 서비스

    /**
     * @method startCrawling
     * @description 현재 인증된 사용자에 대한 크롤링 프로세스를 시작하는 엔드포인트입니다.
     *              HTTP POST 요청을 "/api/crawling/start" 경로로 매핑합니다.
     *              `@AuthenticationPrincipal`을 통해 현재 로그인한 사용자의 정보를 가져와 크롤링 서비스에 전달합니다.
     *              크롤링 작업은 비동기적으로 수행되며, 진행 상황은 WebSocket을 통해 클라이언트에 실시간으로 전달됩니다.
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보 (Spring Security에서 주입)
     */
    @PostMapping("/start")
    public void startCrawling(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername(); // UserDetails 객체에서 현재 로그인한 사용자의 ID를 가져옵니다.
        crawlingService.startCrawling(userId); // CrawlingService를 호출하여 크롤링 작업을 시작합니다.
    }
}