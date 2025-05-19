package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Concert {
    private long concertId;
    private String concertName;
    private String address;
}
