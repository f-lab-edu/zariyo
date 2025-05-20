package com.zariyo.concert.application.facade;

import com.zariyo.concert.api.request.ReserveSeatRequest;
import com.zariyo.concert.api.response.ConcertInfoResponse;
import com.zariyo.concert.api.response.ReservationResponse;
import com.zariyo.concert.application.dto.ConcertSeatDto;
import com.zariyo.concert.application.service.FindConcertService;
import com.zariyo.concert.application.service.ReserveConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConcertFacade {

    private final FindConcertService findConcertService;
    private final ReserveConcertService reserveConcertService;

    public ConcertInfoResponse getConcertInfo(long concertId) {
        return ConcertInfoResponse.builder()
                .concert(findConcertService.getConcertBasicInfo(concertId))
                .schedules(findConcertService.getSchedules(concertId))
                .gradePrices(findConcertService.getSeatPrices(concertId))
                .files(findConcertService.getConcertImageFiles(concertId))
                .build();
    }

    public List<ConcertSeatDto> getAvailableConcertSeats(long scheduleId) {
        return findConcertService.getAvailableConcertSeats(scheduleId);
    }

    public ReservationResponse reserveConcertSeats(ReserveSeatRequest reserveSeats) {
        return ReservationResponse.builder().reservationId(reserveConcertService.reserveConcertSeats(reserveSeats))
                .concert(findConcertService.getConcertBasicInfo(reserveSeats.getConcertId()))
                .schedule(findConcertService.getSchedule(reserveSeats.getScheduleId()))
                .seats(findConcertService.getReservedSeatsPrice(reserveSeats.getScheduleSeatIds()))
                .build();
    }
}
