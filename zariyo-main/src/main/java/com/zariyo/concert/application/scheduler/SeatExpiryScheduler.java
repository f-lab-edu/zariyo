package com.zariyo.concert.application.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.concert.application.producer.SeatsOutboxProducer;
import com.zariyo.concert.domain.entity.ScheduleSeat;
import com.zariyo.concert.domain.entity.SeatsOutbox;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import com.zariyo.concert.infra.SeatReservationRedisRepository;
import com.zariyo.concert.infra.SeatsOutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatExpiryScheduler {

    private final SeatReservationRedisRepository seatReservationRedisRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    @Scheduled(fixedDelay = 10000)
    public void checkExpiredSeats() {
        try {
            Set<String> expiredSeatKeys = seatReservationRedisRepository.getExpiredSeatKeys();

            if (!expiredSeatKeys.isEmpty()) {
                List<Long> expiredSeatIds = expiredSeatKeys.stream()
                        .map(key -> key.replace("seat:hold:", ""))
                        .map(Long::valueOf)
                        .collect(Collectors.toList());

                SeatReservationRelease(expiredSeatIds);

                seatReservationRedisRepository.removeFromExpirySchedule(
                        expiredSeatKeys.toArray(new String[0]));
            }
        } catch (Exception e) {
            log.error("좌석 만료 처리 중 오류 발생", e);
        }
    }

    /**
     * 좌석 해제
     */
    private void SeatReservationRelease(List<Long> seatIds) {
        scheduleSeatRepository.findAllById(seatIds).forEach(ScheduleSeat::release);
    }
}
