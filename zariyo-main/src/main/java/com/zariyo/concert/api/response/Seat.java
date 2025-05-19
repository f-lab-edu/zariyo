package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Seat {
    private long scheduleSeatId;
    private String block;
    private String seatNumber;
    private String grade;
    private BigDecimal price;
}
