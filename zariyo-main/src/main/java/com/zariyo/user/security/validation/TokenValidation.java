package com.zariyo.user.security.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenValidation {

    private final StringRedisTemplate mainRedisTemplate;

    private static final String MAIN_TOKEN_KEY = "main:";
    private static final String MAIN_BLACKLIST_KEY = "blacklist";

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

    public boolean isBlacklisted(String token) {
        String key = MAIN_BLACKLIST_KEY + token;
        Boolean exists = mainRedisTemplate.hasKey(key);
        return exists != null && exists;
    }

    public void saveBlacklistToken(String token, long remaining) {
        mainRedisTemplate.opsForValue().set(MAIN_BLACKLIST_KEY + token, "1", remaining, TimeUnit.MILLISECONDS);
    }
}
