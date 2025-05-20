package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.ConcertFile;

import java.util.List;

public interface ConcertFileRepository {
    List<ConcertFile> findConcertFileByConcertId(long concertId);
}