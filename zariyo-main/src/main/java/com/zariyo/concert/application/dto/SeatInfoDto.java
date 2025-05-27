package com.zariyo.concert.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInfoDto {
    private Long scheduleSeatId;
    private String seatGrade;
    private String block;
    private String seatRow;
    private String seatNumber;
    private BigDecimal price;
    private String status;
}
