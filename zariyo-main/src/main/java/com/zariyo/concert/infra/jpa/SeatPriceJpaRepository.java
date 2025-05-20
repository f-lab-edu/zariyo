package com.zariyo.concert.infra.jpa;

import com.zariyo.concert.domain.entity.ConcertSeatPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatPriceJpaRepository extends JpaRepository<ConcertSeatPrice, Long> {

    List<ConcertSeatPrice> findByConcert_ConcertId(long concertId);

}
