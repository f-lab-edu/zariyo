package com.zariyo.concert.infra.repository;

import com.zariyo.concert.domain.entity.ConcertFile;
import com.zariyo.concert.domain.repository.ConcertFileRepository;
import com.zariyo.concert.infra.jpa.ConcertFileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertFileRepositoryImpl implements ConcertFileRepository {

    private final ConcertFileJpaRepository concertFileJpaRepository;

    @Override
    public List<ConcertFile> findConcertFileByConcertId(long concertId) {
        return concertFileJpaRepository.findByConcert_ConcertId(concertId);
    }
}
