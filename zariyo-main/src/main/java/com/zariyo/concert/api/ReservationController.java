package com.zariyo.concert.api;

import com.zariyo.concert.api.request.ReservationRequest;
import com.zariyo.concert.api.response.ReservationStatusResponse;
import com.zariyo.concert.api.response.ReservationTokenResponse;
import com.zariyo.concert.application.facade.ConcertFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ConcertFacade concertFacade;

    @PostMapping
    public CompletableFuture<ResponseEntity<ReservationTokenResponse>>concertSeatsReservation(@RequestBody ReservationRequest request) {
        return concertFacade.reserveSeats(request.getSeatIds()).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/status")
    public ResponseEntity<ReservationStatusResponse> checkReservationStatus(@RequestParam String token) {
        return ResponseEntity.ok(concertFacade.getReservationStatus(token));
    }
}
