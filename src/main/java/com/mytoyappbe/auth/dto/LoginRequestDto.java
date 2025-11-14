/**
 * @file LoginRequestDto.java
 * @description 사용자 로그인 요청 시 클라이언트로부터 로그인 정보를 받기 위한 DTO(Data Transfer Object)입니다.
 */

package com.mytoyappbe.auth.dto;

import lombok.Data;

/**
 * @class LoginRequestDto
 * @description 사용자 로그인 요청 시 사용되는 DTO입니다.
 *              클라이언트가 HTTP 요청 본문에 사용자 이름과 비밀번호를 담아 보낼 때 이 객체로 매핑됩니다.
 */
@Data // Lombok 어노테이션: getter, setter, equals, hashCode, toString 메서드를 자동으로 생성합니다.
public class LoginRequestDto {
    private String username; // 사용자 로그인 ID
    private String password; // 사용자 비밀번호
}