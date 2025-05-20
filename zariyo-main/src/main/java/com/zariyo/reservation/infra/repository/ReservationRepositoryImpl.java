package com.zariyo.reservation.infra.repository;

import com.zariyo.reservation.domain.entity.Reservation;
import com.zariyo.reservation.domain.repository.ReservationRepository;
import com.zariyo.reservation.infra.jpa.ReservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Long concertReservationSave(Reservation reservation) {
        return reservationJpaRepository.save(reservation).getReservationId();
    }
}
