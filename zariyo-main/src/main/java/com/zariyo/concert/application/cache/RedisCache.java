package com.zariyo.concert.application.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zariyo.concert.application.dto.AvailableSeatsDto;
import com.zariyo.concert.application.dto.ConcertDetailDTO;
import com.zariyo.concert.application.dto.ConcertListDTO;
import com.zariyo.concert.application.dto.SeatInfoDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum RedisCache {
    CONCERTS("concerts", Duration.ofHours(24), new TypeReference<ConcertListDTO>() {}),
    CONCERT_DETAIL("concert-detail", Duration.ofHours(24), new TypeReference<ConcertDetailDTO>() {}),
    AVAILABLE_SEATS("available-seats", Duration.ofSeconds(3), new TypeReference<AvailableSeatsDto>() {}),
    ALL_SEATS("all-seats", Duration.ZERO, new TypeReference<List<SeatInfoDto>>() {}); // TTL 없음

    private final String cacheName;
    private final Duration expiredAfterWrite;
    private final TypeReference<?> typeRef;
}
