package com.mytoyappbe.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CrawlingScheduleDto {
    private String cronExpression;
}
