package com.zariyo.concert.application.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ScheduleInfo {
    private Long scheduleId;
    private LocalDateTime scheduleDateTime;
}
