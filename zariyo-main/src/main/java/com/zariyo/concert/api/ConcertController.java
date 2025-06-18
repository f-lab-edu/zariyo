package com.zariyo.concert.api;

import com.zariyo.concert.api.response.AvailableSeatsResponse;
import com.zariyo.concert.api.response.ConcertDetailResponse;
import com.zariyo.concert.api.response.ConcertListResponse;
import com.zariyo.concert.application.facade.ConcertFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertFacade concertFacade;

    @GetMapping
    public CompletableFuture<ResponseEntity<ConcertListResponse>> getConcerts(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "upcoming") String sort) {
        return CompletableFuture.supplyAsync(() ->
                concertFacade.getConcerts(pageable, sort, categoryId)
        ).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{concertId}")
    public CompletableFuture<ResponseEntity<ConcertDetailResponse>> getConcertDetail(@PathVariable Long concertId) {
        return CompletableFuture.supplyAsync(() ->
                concertFacade.getConcertDetail(concertId)
        ).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/schedules/{scheduleId}/seats")
    public CompletableFuture<ResponseEntity<AvailableSeatsResponse>> getAvailableSeats(@PathVariable Long scheduleId) {
        return concertFacade.getAvailableSeatsAsync(scheduleId)
                .thenApply(ResponseEntity::ok);
    }
}
