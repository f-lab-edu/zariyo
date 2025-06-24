package com.zariyo.concert.application.facade;

import com.zariyo.concert.api.response.*;
import com.zariyo.concert.application.dto.AvailableSeatsDto;
import com.zariyo.concert.application.dto.ReservationStatusDto;
import com.zariyo.concert.application.dto.SeatInfoDto;
import com.zariyo.concert.application.service.ConcertQueryService;
import com.zariyo.concert.application.service.ReservationService;
import com.zariyo.concert.application.service.ScheduleSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ConcertFacade {

    private final ConcertQueryService concertQueryService;
    private final ScheduleSeatService scheduleSeatService;
    private final ReservationService reservationService;

    public ConcertListResponse getConcerts(Pageable pageable, String sortType, Long categoryId) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                SortType.fromValue(sortType)
        );
        return ConcertListResponse.from(concertQueryService.getConcerts(sortedPageable, categoryId));
    }

    public ConcertDetailResponse getConcertDetail(Long concertId) {
        return ConcertDetailResponse.from(concertQueryService.getConcertDetail(concertId));
    }

    public CompletableFuture<AvailableSeatsResponse> getAvailableSeatsAsync(Long scheduleId) {
        CompletableFuture<AvailableSeatsDto> availableSeatsTask =
                CompletableFuture.supplyAsync(() -> scheduleSeatService.getAvailableSeats(scheduleId));

        CompletableFuture<List<SeatInfoDto>> allSeatsTask =
                CompletableFuture.supplyAsync(() -> scheduleSeatService.getAllSeats(scheduleId));

        return availableSeatsTask.thenCombine(allSeatsTask, AvailableSeatsResponse::from);
    }

    public CompletableFuture<ReservationTokenResponse> reserveSeats(List<Long> seatIds) {
        String reservationToken = UUID.randomUUID().toString();
        return CompletableFuture.supplyAsync(() -> {
            reservationService.reserveSeats(seatIds, reservationToken);
            return ReservationTokenResponse.builder()
                    .token(reservationToken)
                    .redirectUrl("/api/reservations/status?token=" + reservationToken)
                    .build();
        });
    }

    public ReservationStatusResponse getReservationStatus(String token) {
        ReservationStatusDto response = reservationService.getReservationStatus(token);
        return response != null
                ? ReservationStatusResponse.from(response)
                : ReservationStatusResponse.nullResponse(token);
    }
}
