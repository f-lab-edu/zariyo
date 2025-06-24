package com.zariyo.concert.infra;

import com.zariyo.concert.config.ReservationLuaScriptManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SeatReservationRedisRepository {


    private final StringRedisTemplate mainRedisTemplate;
    private final ReservationLuaScriptManager luaScriptManager;

    private static final String SEAT_HOLD_PREFIX = "seat:hold:";
    private static final String SEAT_EXPIRY_SCHEDULE_KEY = "seat:expiry:schedule";
    private static final int SEAT_TTL_MINUTES = 5;

    /**
     * 좌석 선점 시도
     */
    public boolean attemptSeatReservation(List<Long> seatIds, String reservationToken) {
        List<String> keys = seatIds.stream()
                .map(id -> SEAT_HOLD_PREFIX + id)
                .collect(Collectors.toList());

        return Boolean.TRUE.equals(mainRedisTemplate.execute(
                luaScriptManager.getScript("reserveSeats", Boolean.class),
                keys,
                reservationToken,
                String.valueOf(SEAT_TTL_MINUTES * 60),
                String.valueOf(System.currentTimeMillis())
        ));
    }

    /**
     * 만료된 좌석 목록 조회
     */
    public Set<String> getExpiredSeatKeys() {
        long currentTime = System.currentTimeMillis();
        return mainRedisTemplate.opsForZSet()
                .rangeByScore(SEAT_EXPIRY_SCHEDULE_KEY, 0, currentTime);
    }

    /**
     * 만료 스케줄에서 좌석 제거
     */
    public void removeFromExpirySchedule(String[] seatKeys) {
        mainRedisTemplate.opsForZSet().remove(SEAT_EXPIRY_SCHEDULE_KEY, (Object[]) seatKeys);
    }
}
