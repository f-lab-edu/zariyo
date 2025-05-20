package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.Concert;

import java.util.Optional;

public interface ConcertRepository {
    Optional<Concert> findConcertById(long concertId);
}
