package com.mytoyappbe.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 클라이언트(브라우저)로부터 전송되는 웹 푸시 구독 정보를 담는 데이터 전송 객체(DTO)입니다.
 * <p>
 * 이 DTO는 웹 푸시 서비스에 구독하기 위해 필요한 정보를 캡슐화하며,
 * 주로 {@link com.mytoyappbe.controller.WebPushController}에서 요청 본문을 받을 때 사용됩니다.
 * Lombok의 {@code @Getter}와 {@code @Setter} 어노테이션을 사용하여 필드의 Getter/Setter 메서드를 자동으로 생성합니다.
 */
public class PushSubscriptionDto {

    /**
     * 푸시 서비스 엔드포인트 URL입니다.
     * 이 URL로 푸시 메시지를 전송하면 클라이언트 브라우저에 알림이 전달됩니다.
     */
    @Getter
    @Setter
    private String endpoint;

    /**
     * 푸시 알림을 받을 사용자의 고유 ID입니다.
     * 이 ID를 통해 특정 사용자에게 알림을 보낼 수 있습니다.
     */
    @Getter
    @Setter
    private String userId;

    /**
     * 푸시 구독과 관련된 암호화 키 정보를 담는 중첩 클래스입니다.
     * 이 키들은 푸시 메시지를 암호화하고 복호화하는 데 사용됩니다.
     */
    @Getter
    @Setter
    private Keys keys;

    /**
     * 푸시 구독 암호화에 사용되는 키들을 정의하는 중첩 정적 클래스입니다.
     */
    @Getter
    @Setter
    public static class Keys {
        /**
         * P-256 타원 곡선 디지털 서명 알고리즘(ECDSA) 공개 키입니다.
         * 이 키는 푸시 메시지를 암호화하는 데 사용됩니다.
         */
        private String p256dh;
        /**
         * 인증 비밀 키입니다.
         * 이 키는 푸시 메시지의 무결성을 검증하는 데 사용됩니다.
         */
        private String auth;
    }
}
