package com.zariyo.concert.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GradePriceDto {
    private String grade;
    private BigDecimal price;
}
