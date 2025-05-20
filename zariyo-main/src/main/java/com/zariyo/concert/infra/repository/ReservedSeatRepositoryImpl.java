package com.zariyo.concert.infra.repository;

import com.zariyo.concert.domain.entity.ReservedSeat;
import com.zariyo.concert.domain.repository.ReservedSeatRepository;
import com.zariyo.concert.infra.jpa.ReservedSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservedSeatRepositoryImpl implements ReservedSeatRepository {

    private final ReservedSeatJpaRepository reservedSeatJpaRepository;

    @Override
    public void saveReserveSeats(List<ReservedSeat> reservedSeas) {
        reservedSeatJpaRepository.saveAll(reservedSeas);
    }
}
