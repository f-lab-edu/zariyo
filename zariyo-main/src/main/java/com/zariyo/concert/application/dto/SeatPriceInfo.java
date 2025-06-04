package com.zariyo.concert.application.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class SeatPriceInfo {
    private String seatGrade;
    private BigDecimal price;
}
