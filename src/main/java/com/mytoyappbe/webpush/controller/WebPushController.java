/**
 * @file WebPushController.java
 * @description 웹 푸시(Web Push) 구독 관련 REST API 요청을 처리하는 컨트롤러입니다.
 *              클라이언트로부터 푸시 구독 정보를 받아 저장하고 관리하는 엔드포인트를 제공합니다.
 */

package com.mytoyappbe.webpush.controller;

import com.mytoyappbe.webpush.dto.PushSubscriptionDto;
import com.mytoyappbe.webpush.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @class WebPushController
 * @description 웹 푸시 구독 관련 HTTP 요청을 처리하는 REST 컨트롤러입니다.
 *              `/api/push` 경로로 들어오는 요청을 매핑합니다.
 */
@RestController // RESTful 웹 서비스 컨트롤러임을 나타냅니다.
@RequestMapping("/api/push") // 이 컨트롤러의 모든 핸들러 메서드는 "/api/push" 경로를 기본으로 합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class WebPushController {

    private final WebPushService webPushService; // 웹 푸시 구독 정보를 저장하고 관리하는 서비스

    /**
     * @method subscribe
     * @description 클라이언트로부터 푸시 구독 요청을 받아 처리하는 엔드포인트입니다.
     *              요청 본문에 포함된 {@link PushSubscriptionDto}와 인증된 사용자의 ID를 사용하여
     *              푸시 구독 정보를 저장합니다.
     * @param {PushSubscriptionDto} subscription - 클라이언트로부터 전송된 푸시 구독 정보 (endpoint, keys 등 포함)
     * @param {UserDetails} userDetails - 현재 인증된 사용자의 상세 정보 (Spring Security에서 주입)
     */
    @PostMapping("/subscribe") // HTTP POST 요청을 "/api/push/subscribe" 경로에 매핑합니다.
    @ResponseStatus(HttpStatus.CREATED) // 요청이 성공적으로 처리되었을 때 HTTP 201 Created 상태 코드를 반환합니다.
    public void subscribe(@RequestBody PushSubscriptionDto subscription, @AuthenticationPrincipal UserDetails userDetails) {
        // WebPushService를 호출하여 푸시 구독 정보를 저장합니다.
        // @AuthenticationPrincipal을 통해 현재 로그인한 사용자의 ID를 가져와 구독 정보와 함께 저장합니다.
        webPushService.saveSubscription(subscription, userDetails.getUsername());
    }
}