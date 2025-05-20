package com.zariyo.concert.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ConcertSeatDto {
    private long scheduleSeatId;
    private String block;
    private int seatNumber;
    private String grade;
    private BigDecimal price;
}
