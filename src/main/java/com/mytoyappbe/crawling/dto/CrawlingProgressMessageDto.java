package com.mytoyappbe.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlingProgressMessageDto {
    private String userId; // userId 필드 추가
    private String title;
    private String content;
    private String status; // e.g., "in_progress", "complete"
    private int progress; // 진행률 필드 추가
}
