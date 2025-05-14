package com.zariyo.user.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class UserRedisRepository {

    private final StringRedisTemplate mainRedisTemplate;

    private static final String MAIN_TOKEN_KEY = "main:";
    private static final String MAIN_TOKEN_SET_KEY = "mainUsers";
    private static final String MAIN_COUNTER_KEY = "main:current";
    private static final String MAIN_BLACKLIST_KEY = "blacklist";

    public void expireToken(String jwtToken, String queueToken, long remaining) {
        // 블랙리스트 등록
        mainRedisTemplate.opsForValue().set(MAIN_BLACKLIST_KEY + jwtToken, "1", remaining, TimeUnit.MILLISECONDS);

        // 메인 Redis 에서 토큰 삭제
        mainRedisTemplate.opsForSet().remove(MAIN_TOKEN_SET_KEY, queueToken);
        mainRedisTemplate.delete(MAIN_TOKEN_KEY + queueToken);
        mainRedisTemplate.opsForValue().decrement(MAIN_COUNTER_KEY);
    }
}
