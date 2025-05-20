package com.zariyo.concert.infra.repository;

import com.zariyo.concert.domain.entity.Concert;
import com.zariyo.concert.domain.repository.ConcertRepository;
import com.zariyo.concert.infra.jpa.ConcertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {

    private final ConcertJpaRepository concertJpaRepository;

    @Override
    public Optional<Concert> findConcertById(long concertId) {
        return concertJpaRepository.findById(concertId);
    }
}
