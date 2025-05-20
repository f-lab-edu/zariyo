package com.zariyo.concert.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveSeatRequest {
    private long concertId;
    private long scheduleId;
    private List<Long> scheduleSeatIds;
}
