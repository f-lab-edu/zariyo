package com.zariyo.concert.infra.repository;

import com.zariyo.concert.domain.entity.ScheduleSeatList;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import com.zariyo.concert.infra.jpa.ScheduleSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleSeatRepositoryImpl implements ScheduleSeatRepository {
    
    private final ScheduleSeatJpaRepository scheduleSeatJpaRepository;
    
    @Override
    public List<ScheduleSeatList> findAvailableSeatsByScheduleId(long scheduleId) {
        return scheduleSeatJpaRepository.findAvailableSeatsByScheduleId(scheduleId);
    }

    @Override
    public List<ScheduleSeatList> findByScheduleSeatIdIn(List<Long> scheduleSeatIds) {
        return scheduleSeatJpaRepository.findByScheduleSeatIdIn(scheduleSeatIds);
    }
}
