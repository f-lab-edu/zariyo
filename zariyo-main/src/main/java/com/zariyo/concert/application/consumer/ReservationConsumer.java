package com.zariyo.concert.application.consumer;

import com.zariyo.concert.application.consumer.event.ReservationRequestEvent;
import com.zariyo.concert.application.service.ReservationService;
import com.zariyo.concert.domain.entity.ScheduleSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationConsumer {

    private final ReservationService reservationService;

    @KafkaListener(topics = "reservation-requests", groupId = "zariyo-group")
    public void consumeReservationRequest(ReservationRequestEvent event) {
        reservationService.updateSeatHold(event.getSeatIds());
    }
}
