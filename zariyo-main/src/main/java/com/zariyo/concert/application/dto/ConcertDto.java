package com.zariyo.concert.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConcertDto {
    private long concertId;
    private String title;
    private String address;
}
