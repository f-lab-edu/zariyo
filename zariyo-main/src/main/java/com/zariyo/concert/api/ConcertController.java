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

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertFacade concertFacade;

    @GetMapping
    public ResponseEntity<ConcertListResponse> getConcerts(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "upcoming") String sort) {
        return ResponseEntity.ok(concertFacade.getConcerts(pageable, sort, categoryId));
    }

    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertDetailResponse> getConcertDetail(@PathVariable Long concertId) {
        return ResponseEntity.ok(concertFacade.getConcertDetail(concertId));
    }

    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<AvailableSeatsResponse> getAvailableSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(concertFacade.getAvailableSeats(scheduleId));
    }
}
