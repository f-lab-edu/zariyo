package com.zariyo.concert.infra.jpa;

import com.zariyo.concert.domain.entity.ScheduleSeatList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScheduleSeatJpaRepository extends JpaRepository<ScheduleSeatList, Long> {

    @Query("""
        SELECT ssl
        FROM ScheduleSeatList ssl
        JOIN FETCH ssl.concertSeatPrice
        WHERE ssl.schedule.scheduleId = :scheduleId
          AND ssl.scheduleSeatId NOT IN (
              SELECT rs.scheduleSeat.scheduleSeatId
              FROM ReservedSeat rs
          )
    """)
    List<ScheduleSeatList> findAvailableSeatsByScheduleId(long scheduleId);

    @EntityGraph(attributePaths = "concertSeatPrice")
    List<ScheduleSeatList> findByScheduleSeatIdIn(List<Long> scheduleSeatIds);
}
