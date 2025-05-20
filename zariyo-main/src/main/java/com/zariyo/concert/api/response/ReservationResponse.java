package com.zariyo.concert.api.response;

import com.zariyo.concert.application.dto.ConcertDto;
import com.zariyo.concert.application.dto.ConcertSeatDto;
import com.zariyo.concert.application.dto.ScheduleDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationResponse {
    private long reservationId;
    private ConcertDto concert;
    private ScheduleDto schedule;
    private List<ConcertSeatDto> seats;
}
