package com.zariyo.concert.application.service;

import com.zariyo.concert.application.dto.AvailableSeatsDto;
import com.zariyo.concert.application.dto.SeatInfoDto;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleSeatService {

    private final ScheduleSeatRepository scheduleSeatRepository;

    @Cacheable(
        cacheManager = "localCacheManager",
        value = "available-seats",
        key = "#scheduleId"
    )
    public AvailableSeatsDto getAvailableSeats(Long scheduleId) {
        return AvailableSeatsDto.builder()
                .scheduleId(scheduleId)
                .availableSeats(
                        scheduleSeatRepository.findAvailableSeatsByScheduleId(scheduleId).stream()
                        .map(scheduleSeat -> SeatInfoDto.builder()
                                .scheduleSeatId(scheduleSeat.getScheduleSeatId())
                                .seatGrade(scheduleSeat.getSeatGrade())
                                .block(scheduleSeat.getHallSeat().getBlock())
                                .seatRow(scheduleSeat.getHallSeat().getSeatRow())
                                .seatNumber(scheduleSeat.getHallSeat().getSeatNumber())
                                .price(scheduleSeat.getPrice())
                                .status(scheduleSeat.getStatus().name())
                                .build())
                        .toList()
                )
                .build();
    }

    @Cacheable(
        cacheManager = "redisCacheManager",
        value = "all-seats",
        key = "#scheduleId"
    )
    public List<SeatInfoDto> getAllSeats(Long scheduleId) {
        return scheduleSeatRepository.findAllSeatsByScheduleId(scheduleId).stream()
                .map(scheduleSeat -> SeatInfoDto.builder()
                        .scheduleSeatId(scheduleSeat.getScheduleSeatId())
                        .seatGrade(scheduleSeat.getSeatGrade())
                        .block(scheduleSeat.getHallSeat().getBlock())
                        .seatRow(scheduleSeat.getHallSeat().getSeatRow())
                        .seatNumber(scheduleSeat.getHallSeat().getSeatNumber())
                        .price(scheduleSeat.getPrice())
                        .status(scheduleSeat.getStatus().name())
                        .build())
                .toList();
    }
}
