package com.zariyo.concert.infra.repository;

import com.zariyo.concert.domain.entity.ConcertSeatPrice;
import com.zariyo.concert.domain.repository.SeatPriceRepository;
import com.zariyo.concert.infra.jpa.SeatPriceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeatPriceRepositoryImpl implements SeatPriceRepository {

    private final SeatPriceJpaRepository seatPriceJpaRepository;

    @Override
    public List<ConcertSeatPrice> findSeatPriceByConcertId(long concertId) {
        return seatPriceJpaRepository.findByConcert_ConcertId(concertId);
    }
}
