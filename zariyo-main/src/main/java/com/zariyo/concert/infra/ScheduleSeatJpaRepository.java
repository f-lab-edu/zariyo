package com.zariyo.concert.infra;

import com.zariyo.concert.domain.entity.ScheduleSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleSeatJpaRepository extends JpaRepository<ScheduleSeat, Long> {
    
    @Query("SELECT ss FROM ScheduleSeat ss " +
           "JOIN FETCH ss.hallSeat hs " +
           "WHERE ss.schedule.scheduleId = :scheduleId " +
           "AND ss.status = 'AVAILABLE' " +
           "ORDER BY hs.seatGrade, hs.block, hs.seatRow, hs.seatNumber")
    List<ScheduleSeat> findAvailableSeatsByScheduleId(@Param("scheduleId") Long scheduleId);
    
    @Query("SELECT ss FROM ScheduleSeat ss " +
           "JOIN FETCH ss.hallSeat hs " +
           "WHERE ss.schedule.scheduleId = :scheduleId " +
           "ORDER BY hs.block, hs.seatRow, hs.seatNumber")
    List<ScheduleSeat> findAllSeatsByScheduleId(@Param("scheduleId") Long scheduleId);
}
