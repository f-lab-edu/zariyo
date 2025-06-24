package com.zariyo.concert.application.consumer.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestEvent {
    private String reservationToken;
    private List<Long> seatIds;
    private String eventType;
    private long timestamp;
}
