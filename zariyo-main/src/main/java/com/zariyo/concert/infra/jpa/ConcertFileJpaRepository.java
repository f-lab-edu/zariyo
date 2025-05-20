package com.zariyo.concert.infra.jpa;

import com.zariyo.concert.domain.entity.ConcertFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertFileJpaRepository extends JpaRepository<ConcertFile, Long> {

    List<ConcertFile> findByConcert_ConcertId(long concertId);

}
