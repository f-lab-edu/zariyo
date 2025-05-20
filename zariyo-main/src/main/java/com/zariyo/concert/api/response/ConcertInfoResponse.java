package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ConcertInfoResponse {
    private Concert concert;
    private String address;
    private List<Schedule> schedules;
    private List<GradePrice> gradePrices;
    private List<ConcertFile> files;
}
