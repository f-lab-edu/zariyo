package com.zariyo.reservation.infra.jpa;

import com.zariyo.reservation.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    // Custom query methods can be defined here if needed
}
