package com.zariyo.concert.application.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatusDto {
    private String reservationToken;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
