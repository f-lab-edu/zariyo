package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationResponse {
    private long reservationId;
    private Concert concert;
    private Schedule schedule;
    private List<Seat> seats;
}
