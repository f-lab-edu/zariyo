package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.ScheduleSeat;

import java.util.List;

public interface ScheduleSeatRepository {
    List<ScheduleSeat> findAvailableSeatsByScheduleId(Long scheduleId);
    List<ScheduleSeat> findAllSeatsByScheduleId(Long scheduleId);
}
