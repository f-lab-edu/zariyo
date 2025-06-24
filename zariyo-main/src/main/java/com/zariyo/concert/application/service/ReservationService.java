package com.zariyo.concert.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.concert.application.dto.ReservationStatusDto;
import com.zariyo.concert.application.producer.SeatsOutboxProducer;
import com.zariyo.concert.domain.entity.ScheduleSeat;
import com.zariyo.concert.domain.entity.SeatsOutbox;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import com.zariyo.concert.infra.ReservationStatusRedisRepository;
import com.zariyo.concert.infra.SeatReservationRedisRepository;
import com.zariyo.concert.infra.SeatsOutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final SeatsOutboxJpaRepository seatsOutboxJpaRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final SeatReservationRedisRepository seatReservationRedisRepository;
    private final ReservationStatusRedisRepository reservationStatusRedisRepository;

    private final SeatsOutboxProducer seatsOutboxProducer;
    private final ObjectMapper objectMapper;

    private String createReservationPayload(List<Long> seatIds) {
        try {
            return objectMapper.writeValueAsString(seatIds);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize seat IDs", e);
        }
    }

    @Transactional
    public void reserveSeats(List<Long> seatIds, String reservationToken) {

        boolean seatReservationSuccess = seatReservationRedisRepository.attemptSeatReservation(seatIds, reservationToken);

        if (seatReservationSuccess) {
            SeatsOutbox outbox = SeatsOutbox.builder()
                    .reservationToken(reservationToken)
                    .seatIds(createReservationPayload(seatIds))
                    .status(SeatsOutbox.OutboxStatus.PENDING)
                    .build();
            SeatsOutbox savedOutbox = seatsOutboxJpaRepository.save(outbox);

            reservationStatusRedisRepository.setReservationStatus(
                    reservationToken, "SUCCESS", "좌석 예약이 완료되었습니다.");

            seatsOutboxProducer.sendToKafkaAsync(savedOutbox);
        } else {
            reservationStatusRedisRepository.setReservationStatus(
                    reservationToken, "FAILED", "이미 예약된 좌석이 포함되어 있습니다.");
        }
    }

    @Transactional
    public void updateSeatHold(List<Long> seatIds) {
        scheduleSeatRepository.findAllById(seatIds).forEach(ScheduleSeat::hold);
    }

    public ReservationStatusDto getReservationStatus(String reservationToken) {
        return reservationStatusRedisRepository.getReservationStatus(reservationToken);
    }
}
