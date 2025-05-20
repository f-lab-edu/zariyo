package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.ScheduleSeatList;

import java.util.List;

public interface ScheduleSeatRepository {
    List<ScheduleSeatList> findAvailableSeatsByScheduleId(long scheduleId);

    List<ScheduleSeatList> findByScheduleSeatIdIn(List<Long> scheduleSeatIds);
}
