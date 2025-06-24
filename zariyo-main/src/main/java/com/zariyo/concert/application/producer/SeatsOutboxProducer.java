package com.zariyo.concert.application.producer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.concert.application.consumer.event.ReservationRequestEvent;
import com.zariyo.concert.domain.entity.ScheduleSeat;
import com.zariyo.concert.domain.entity.SeatsOutbox;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import com.zariyo.concert.infra.SeatsOutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatsOutboxProducer {

    private final KafkaTemplate<String, ReservationRequestEvent> kafkaTemplate;
    private final SeatsOutboxJpaRepository seatsOutboxJpaRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final ObjectMapper objectMapper;

    public void sendToKafkaAsync(SeatsOutbox outbox) {
        try {
            ReservationRequestEvent event = createKafkaEvent(outbox);
                
            kafkaTemplate.send("reservation-requests", outbox.getReservationToken(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            updateOutboxToSent(outbox);
                        } else {
                            handleKafkaFailure(outbox);
                        }
                    });
        } catch (Exception e) {
            handleKafkaFailure(outbox);
        }
    }

    private ReservationRequestEvent createKafkaEvent(SeatsOutbox outbox) {
        try {
            List<Long> seatIds = objectMapper.readValue(
                outbox.getSeatIds(), new TypeReference<List<Long>>() {});

            return ReservationRequestEvent.builder()
                .reservationToken(outbox.getReservationToken())
                .seatIds(seatIds)
                .timestamp(System.currentTimeMillis())
                .build();
        } catch (Exception e) {
            log.error("Kafka 이벤트 생성 실패: token={}", outbox.getReservationToken(), e);
            throw new RuntimeException("Kafka 이벤트 생성 실패", e);
        }
    }

    private void updateOutboxToSent(SeatsOutbox outbox) {
        try {
            outbox.markAsSent();
            seatsOutboxJpaRepository.save(outbox);
        } catch (Exception e) {
            log.error("Outbox 상태 업데이트 실패: ReservationToken={}", outbox.getReservationToken(), e);
        }
    }

    private void handleKafkaFailure(SeatsOutbox outbox) {
        try {
            List<Long> seatIds = objectMapper.readValue(
                outbox.getSeatIds(), new TypeReference<List<Long>>() {});

            scheduleSeatRepository.findAllById(seatIds).forEach(ScheduleSeat::hold);
            
            outbox.markAsFailed();
            seatsOutboxJpaRepository.save(outbox);
            
            log.error("Kafka 전송 실패 - 직접 DB 업데이트 수행: token={}", outbox.getReservationToken());
        } catch (Exception ex) {
            log.error("Kafka 전송 실패 처리 중 추가 오류: ReservationToken={}", outbox.getReservationToken(), ex);
        }
    }
}
