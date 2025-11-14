/**
 * @file TokenInfo.java
 * @description JWT(JSON Web Token)의 Access Token과 Refresh Token 정보를 캡슐화하는 데이터 전송 객체(DTO)입니다.
 *              주로 사용자 로그인 및 토큰 재발급 응답으로 사용됩니다.
 */

package com.mytoyappbe.auth.config.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @class TokenInfo
 * @description JWT 토큰 정보를 담는 DTO 클래스입니다.
 *              `grantType`, `accessToken`, `refreshToken` 필드를 포함합니다.
 */
@Data // Lombok 어노테이션: getter, setter, toString, equals, hashCode 메서드를 자동으로 생성합니다.
@Builder // Lombok 어노테이션: 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
@AllArgsConstructor // Lombok 어노테이션: 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
public class TokenInfo {
    private String grantType;    // 토큰의 유형 (예: "Bearer")
    private String accessToken;  // 실제 인증에 사용되는 Access Token
    private String refreshToken; // Access Token 만료 시 재발급에 사용되는 Refresh Token
}