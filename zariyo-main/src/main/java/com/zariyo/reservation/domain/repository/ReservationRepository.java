package com.zariyo.reservation.domain.repository;

import com.zariyo.reservation.domain.entity.Reservation;

public interface ReservationRepository {

    Long concertReservationSave(Reservation reservation);

}
