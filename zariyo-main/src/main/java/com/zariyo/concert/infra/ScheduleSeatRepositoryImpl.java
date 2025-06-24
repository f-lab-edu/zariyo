package com.zariyo.concert.infra;

import com.zariyo.concert.domain.entity.ScheduleSeat;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleSeatRepositoryImpl implements ScheduleSeatRepository {
    
    private final ScheduleSeatJpaRepository scheduleSeatJpaRepository;
    
    @Override
    public List<ScheduleSeat> findAvailableSeatsByScheduleId(Long scheduleId) {
        return scheduleSeatJpaRepository.findAvailableSeatsByScheduleId(scheduleId);
    }

    @Override
    public List<ScheduleSeat> findAllSeatsByScheduleId(Long scheduleId) {
        return scheduleSeatJpaRepository.findAllSeatsByScheduleId(scheduleId);
    }

    @Override
    public List<ScheduleSeat> findAllById(List<Long> seatIds) {
        return scheduleSeatJpaRepository.findAllById(seatIds);
    }
}
