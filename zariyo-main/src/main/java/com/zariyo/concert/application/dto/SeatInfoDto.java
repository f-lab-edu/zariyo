package com.zariyo.concert.application.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
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
