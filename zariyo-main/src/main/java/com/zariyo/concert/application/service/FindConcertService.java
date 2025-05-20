package com.zariyo.concert.application.service;

import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.ConcertNotFoundException;
import com.zariyo.concert.application.dto.*;
import com.zariyo.concert.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindConcertService {

    private final ConcertRepository concertRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final SeatPriceRepository seatPriceRepository;
    private final ConcertFileRepository concertFileRepository;

    public ConcertDto getConcertBasicInfo(Long concertId) {
        return concertRepository.findConcertById(concertId)
                .map(concert -> ConcertDto.builder()
                        .concertId(concert.getConcertId())
                        .title(concert.getTitle())
                        .address(concert.getConcertHall().getAddress())
                        .build())
                .orElseThrow(() -> new ConcertNotFoundException(ErrorCode.CONCERT_NOT_FOUND));
    }

    public List<ScheduleDto> getSchedules(long concertId) {
        return scheduleRepository.findScheduleByConcertId(concertId).stream()
                .map(schedule -> ScheduleDto.builder()
                        .scheduleId(schedule.getScheduleId())
                        .scheduleDateTime(schedule.getScheduleDateTime())
                        .build())
                .toList();
    }

    public List<GradePriceDto> getSeatPrices(long concertId) {
        return seatPriceRepository.findSeatPriceByConcertId(concertId).stream()
                .map(gradePrice -> GradePriceDto.builder()
                        .grade(gradePrice.getSeatGrade())
                        .price(gradePrice.getPrice())
                        .build())
                .toList();
    }

    public List<ConcertFileDto> getConcertImageFiles(long concertId) {
        return concertFileRepository.findConcertFileByConcertId(concertId).stream()
                .map(concertFile -> ConcertFileDto.builder()
                        .fileId(concertFile.getFileId())
                        .fileName(concertFile.getOriginFileName())
                        .filePath(concertFile.getFilePath())
                        .fileType(concertFile.getFileType())
                        .build())
                .toList();
    }

    public List<ConcertSeatDto> getAvailableConcertSeats(long scheduleId) {
        return scheduleSeatRepository.findAvailableSeatsByScheduleId(scheduleId).stream()
                .map(scheduleSeat -> ConcertSeatDto.builder()
                        .scheduleSeatId(scheduleSeat.getScheduleSeatId())
                        .block(scheduleSeat.getBlock())
                        .seatNumber(scheduleSeat.getSeatNumber())
                        .grade(scheduleSeat.getSeatGrade())
                        .price(scheduleSeat.getConcertSeatPrice().getPrice())
                        .build())
                .toList();
    }

    public ScheduleDto getSchedule(long scheduleId) {
        return scheduleRepository.findScheduleById(scheduleId)
                .map(schedule -> ScheduleDto.builder()
                        .scheduleId(schedule.getScheduleId())
                        .scheduleDateTime(schedule.getScheduleDateTime())
                        .build())
                .orElseThrow(() -> new ConcertNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    public List<ConcertSeatDto> getReservedSeatsPrice(List<Long> scheduleSeatIds) {
        return scheduleSeatRepository.findByScheduleSeatIdIn(scheduleSeatIds).stream()
                .map(scheduleSeat -> ConcertSeatDto.builder()
                        .scheduleSeatId(scheduleSeat.getScheduleSeatId())
                        .block(scheduleSeat.getBlock())
                        .seatNumber(scheduleSeat.getSeatNumber())
                        .grade(scheduleSeat.getSeatGrade())
                        .price(scheduleSeat.getConcertSeatPrice().getPrice())
                        .build())
                .toList();
    }
}
