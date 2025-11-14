/**
 * @file CrawlingProgressMessageDto.java
 * @description 웹 크롤링 진행 상황을 클라이언트에게 실시간으로 전달하기 위한 데이터 전송 객체(DTO)입니다.
 *              사용자 ID, 메시지 제목, 내용, 현재 상태, 진행률 등의 정보를 포함합니다.
 */

package com.mytoyappbe.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class CrawlingProgressMessageDto
 * @description 크롤링 진행 상황을 나타내는 메시지 DTO입니다.
 *              STOMP over WebSocket을 통해 클라이언트에 전송됩니다.
 */
@Data // Lombok 어노테이션: getter, setter, equals, hashCode, toString 메서드를 자동으로 생성합니다.
@NoArgsConstructor // Lombok 어노테이션: 인자 없는 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor // Lombok 어노테이션: 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
@Builder // Lombok 어노테이션: 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
public class CrawlingProgressMessageDto {
    private String userId;    // 메시지를 수신할 사용자의 ID
    private String title;     // 크롤링 진행 상황 메시지의 제목
    private String content;   // 크롤링 진행 상황 메시지의 내용
    private String status;    // 크롤링 작업의 현재 상태 (예: "IN_PROGRESS", "COMPLETED")
    private int progress;     // 크롤링 진행률 (0-100)
}