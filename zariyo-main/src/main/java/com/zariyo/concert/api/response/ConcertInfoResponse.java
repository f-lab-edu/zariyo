package com.zariyo.concert.api.response;

import com.zariyo.concert.application.dto.ConcertDto;
import com.zariyo.concert.application.dto.ConcertFileDto;
import com.zariyo.concert.application.dto.GradePriceDto;
import com.zariyo.concert.application.dto.ScheduleDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ConcertInfoResponse {
    private ConcertDto concert;
    private List<ScheduleDto> schedules;
    private List<GradePriceDto> gradePrices;
    private List<ConcertFileDto> files;
}
