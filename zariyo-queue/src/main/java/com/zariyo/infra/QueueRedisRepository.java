package com.zariyo.infra;

import com.zariyo.exception.ErrorCode;
import com.zariyo.exception.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String MAIN_KEY = "main:queue";
    private static final String RPS_KEY_PREFIX = "rps:";
    private static final int RPS_THRESHOLD = 500;

    /**
     * 초 단위 RPS 증가 및 임계값 초과 여부 확인
     * @return 임계값 초과시 true 미만시 false
     */
    public boolean isOverRpsThreshold(){
        String key = RPS_KEY_PREFIX + LocalTime.now().getSecond();
        Long rps = redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, Duration.ofSeconds(2));
        return rps != null && rps > RPS_THRESHOLD;
    }

    /**
     * 대기열에서 상위 N개의 토큰 제거 후 반환
     *
     * @param allowCount 가져올 토큰 수
     * @return 제거된 토큰 목록
     */
    public List<String> popTopTokens(int allowCount) {
        return Optional.ofNullable(redisTemplate.opsForZSet().popMin(MAIN_KEY, allowCount))
                .orElse(Collections.emptySet())
                .stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .collect(Collectors.toList());
    }

    /**
     * 현재 대기열에 존재하는 토큰 수 조회
     *
     * @return 대기열 크기
     */
    public int getCurrentQueueSize() {
        Long size = redisTemplate.opsForZSet().size(MAIN_KEY);
        return size != null ? size.intValue() : 0;
    }

    /**
     * 토큰을 대기열에 추가하고 순번 반환
     *
     * @param token 사용자 식별 토큰
     * @return 추가된 토큰의 순번
     */
    public int addToQueue(String token) {
        if (Boolean.TRUE.equals(redisTemplate.opsForZSet().add(MAIN_KEY, token, System.currentTimeMillis()))) {
            return redisTemplate.opsForZSet().rank(MAIN_KEY, token).intValue() + 1;
        }
        throw new QueueException(ErrorCode.ALREADY_IN_QUEUE);
    }

    /**
     * 대기열에서 특정 토큰의 순번 조회
     *
     * @param token 사용자 식별 토큰
     * @return 순번 (0부터 시작)
     */
    public Long getPosition(String token) {
        return redisTemplate.opsForZSet().rank(MAIN_KEY, token);
    }


    /**
     * 대기열에서 특정 토큰 제거
     *
     * @param token 사용자 식별 토큰
     */
    public void removeFromQueue(String token) {
        redisTemplate.opsForZSet().remove(MAIN_KEY, token);
    }

    /**
     * 대기열에 존재하는 모든 토큰 조회
     *
     * @return 대기 중인 토큰 목록
     */
    public List<String> findAllTokens() {
        Set<String> tokens = redisTemplate.opsForZSet().range(MAIN_KEY, 0, -1);

        if (tokens == null || tokens.isEmpty()) {
            log.warn("대기열에 토큰이 존재하지 않습니다.");
            return Collections.emptyList();
        }

        return new ArrayList<>(tokens);
    }

}
