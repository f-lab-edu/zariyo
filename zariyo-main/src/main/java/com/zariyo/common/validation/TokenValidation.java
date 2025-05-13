package com.zariyo.common.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenValidation {

    private final StringRedisTemplate mainRedisTemplate;

    private static final String MAIN_TOKEN_KEY = "main:";

    public boolean validateAndRefresh(String token) {
        String key = MAIN_TOKEN_KEY + token;
        Boolean exists = mainRedisTemplate.hasKey(key);
        if (exists != null && exists) {
            String now = String.valueOf(System.currentTimeMillis());
            mainRedisTemplate.opsForValue().set(key, now);
            return true;
        }
        return false;
    }

}
