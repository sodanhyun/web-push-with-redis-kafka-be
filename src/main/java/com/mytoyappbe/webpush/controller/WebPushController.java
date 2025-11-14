package com.mytoyappbe.webpush.controller;

import com.mytoyappbe.webpush.dto.PushSubscriptionDto;
import com.mytoyappbe.webpush.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 웹 푸시(Web Push) 구독 관련 REST API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 클라이언트(프론트엔드)로부터 푸시 구독 정보를 받아 저장하고 관리하는 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/push") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/push" 경로를 기본으로 합니다.
@RequiredArgsConstructor
public class WebPushController {

    /**
     * 웹 푸시 구독 정보를 저장하고 관리하는 서비스입니다.
     * Spring에 의해 주입됩니다.
     */
    private final WebPushService webPushService;

    /**
     * 클라이언트로부터 푸시 구독 요청을 받아 처리하는 엔드포인트입니다.
     * <p>
     * {@code @PostMapping("/subscribe")} 어노테이션은 HTTP POST 요청을 "/api/push/subscribe" 경로로 매핑합니다.
     * {@code @RequestBody PushSubscriptionDto subscription}은 요청 본문에 포함된 JSON 데이터를
     * {@link PushSubscriptionDto} 객체로 자동 변환하여 받습니다.
     * {@code @ResponseStatus(HttpStatus.CREATED)}는 요청이 성공적으로 처리되었을 때 HTTP 201 Created 상태 코드를 반환하도록 설정합니다.
     *
     * @param subscription 클라이언트로부터 전송된 푸시 구독 정보 (endpoint, keys 등 포함)
     */
    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@RequestBody PushSubscriptionDto subscription, @AuthenticationPrincipal UserDetails userDetails) {
        webPushService.saveSubscription(subscription, userDetails.getUsername());
    }
}
