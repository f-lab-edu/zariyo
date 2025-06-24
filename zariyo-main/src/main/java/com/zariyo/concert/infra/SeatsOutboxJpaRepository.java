package com.zariyo.concert.infra;

import com.zariyo.concert.domain.entity.SeatsOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatsOutboxJpaRepository extends JpaRepository<SeatsOutbox, String> {

    Optional<SeatsOutbox> findByReservationToken(String reservationToken);
}
