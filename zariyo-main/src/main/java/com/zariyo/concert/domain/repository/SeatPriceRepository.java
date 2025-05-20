package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.ConcertSeatPrice;

import java.util.List;

public interface SeatPriceRepository {
    List<ConcertSeatPrice> findSeatPriceByConcertId(long concertId);
}
