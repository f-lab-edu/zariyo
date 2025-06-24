package com.zariyo.concert.application.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxDTO {
    private Long seatsOutboxId;
    private String reservationToken;
    private String payload;
    private OutboxStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public enum OutboxStatus {
        PENDING, SENT, FAILED
    }
}
