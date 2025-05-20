package com.zariyo.concert.infra.jpa;

import com.zariyo.concert.domain.entity.ReservedSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservedSeatJpaRepository extends JpaRepository<ReservedSeat, Long> {
    // Custom query methods can be defined here if needed
}
