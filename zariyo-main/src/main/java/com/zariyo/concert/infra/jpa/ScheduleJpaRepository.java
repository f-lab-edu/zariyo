package com.zariyo.concert.infra.jpa;

import com.zariyo.concert.domain.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleJpaRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByConcert_ConcertId(long concertId);

}
