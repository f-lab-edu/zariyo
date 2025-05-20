package com.zariyo.concert.infra.repository;

import com.zariyo.concert.domain.entity.Schedule;
import com.zariyo.concert.domain.repository.ScheduleRepository;
import com.zariyo.concert.infra.jpa.ScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepository {

    private final ScheduleJpaRepository scheduleJpaRepository;

    @Override
    public List<Schedule> findScheduleByConcertId(long concertId) {
        return scheduleJpaRepository.findByConcert_ConcertId(concertId);
    }

    @Override
    public Optional<Schedule> findScheduleById(long scheduleId) {
        return scheduleJpaRepository.findById(scheduleId);
    }
}
