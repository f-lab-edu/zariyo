package com.zariyo.concert.api.response;

import com.zariyo.concert.application.dto.ReservationStatusDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatusResponse {
    private String reservationToken;
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String redirectUrl;

    public static ReservationStatusResponse from(ReservationStatusDto statusDto) {
        return ReservationStatusResponse.builder()
                .reservationToken(statusDto.getReservationToken())
                .status(statusDto.getStatus())
                .message(statusDto.getMessage())
                .timestamp(statusDto.getTimestamp())
                .redirectUrl("/api/reservations/status?token=" + statusDto.getReservationToken())
                .build();
    }

    public static ReservationStatusResponse nullResponse(String token) {
        return ReservationStatusResponse.builder()
                .reservationToken(token)
                .status("PROCESSING")
                .message("예약 중 입니다.")
                .timestamp(LocalDateTime.now())
                .redirectUrl("/api/reservations/status?token=" + token)
                .build();
    }
}
