/**
 * @file RefreshTokenRequestDto.java
 * @description Refresh Token 재발급 요청 시 클라이언트로부터 Refresh Token을 받기 위한 DTO(Data Transfer Object)입니다.
 */

package com.mytoyappbe.auth.dto;

import lombok.Data;

/**
 * @class RefreshTokenRequestDto
 * @description Refresh Token 재발급 요청 시 사용되는 DTO입니다.
 *              클라이언트가 HTTP 요청 본문에 Refresh Token을 담아 보낼 때 이 객체로 매핑됩니다.
 */
@Data // Lombok 어노테이션: getter, setter, equals, hashCode, toString 메서드를 자동으로 생성합니다.
public class RefreshTokenRequestDto {
    private String refreshToken; // 클라이언트로부터 받은 Refresh Token
}