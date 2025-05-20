package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Schedule {
    private long scheduleId;
    private LocalDateTime concertDate;
}
