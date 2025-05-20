package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GradePrice {
    private String grade;
    private BigDecimal price;
}
