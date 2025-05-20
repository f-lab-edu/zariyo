package com.zariyo.concert.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleDto {
    private long scheduleId;
    private LocalDateTime scheduleDateTime;
}
