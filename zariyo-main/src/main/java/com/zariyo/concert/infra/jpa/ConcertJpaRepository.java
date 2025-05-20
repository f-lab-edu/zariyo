package com.zariyo.concert.infra.jpa;

import com.zariyo.concert.domain.entity.Concert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {

    @EntityGraph(attributePaths = {"concertHall"})
    Optional<Concert> findById(Long concertId);
}
