package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.Schedule;
import io.lettuce.core.Value;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {

    List<Schedule> findScheduleByConcertId(long concertId);

    Optional<Schedule> findScheduleById(long scheduleId);
}
