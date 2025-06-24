package com.zariyo.concert.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.concert.application.dto.ReservationStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReservationStatusRedisRepository {

    private final StringRedisTemplate mainRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String RESERVATION_STATUS_PREFIX = "reservation:status:";
    private static final Duration STATUS_TTL = Duration.ofMinutes(10);

    /**
     * 예약 상태 저장 (성공/실패/처리중)
     */
    public void setReservationStatus(String reservationToken, String status, String message) {
        try {
            ReservationStatusDto statusDto = ReservationStatusDto.builder()
                    .reservationToken(reservationToken)
                    .status(status)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            String statusKey = RESERVATION_STATUS_PREFIX + reservationToken;
            mainRedisTemplate.opsForValue().set(
                    statusKey,
                    objectMapper.writeValueAsString(statusDto),
                    STATUS_TTL
            );

            log.info("예약 상태 저장: token={}, status={}", reservationToken, status);
        } catch (Exception e) {
            log.error("예약 상태 저장 실패: token={}", reservationToken, e);
        }
    }

    /**
     * 예약 상태 조회
     */
    public ReservationStatusDto getReservationStatus(String reservationToken) {
        try {
            String statusKey = RESERVATION_STATUS_PREFIX + reservationToken;
            String statusJson = mainRedisTemplate.opsForValue().get(statusKey);
            return objectMapper.readValue(statusJson, ReservationStatusDto.class);
        } catch (Exception e) {
            log.error("예약 상태 조회 실패: token={}", reservationToken, e);
            return null;
        }
    }
}
