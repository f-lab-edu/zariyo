package com.zariyo.infra;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static com.zariyo.infra.QueueRedisRepository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToeknCleanupScheduler {

    private final StringRedisTemplate mainRedisTemplate;

    @Value("${token.cleanup.period}")
    private long expirationMillis; //10분

    @Scheduled(fixedRate = 5000, initialDelay = 1000)
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        try {
            long now = System.currentTimeMillis();
            mainRedisTemplate.execute((RedisCallback<Void>) connection -> {
                 byte[] setKey = MAIN_TOKEN_SET_KEY.getBytes(StandardCharsets.UTF_8);
                 Cursor<byte[]> cursor = connection.sScan(setKey, ScanOptions.scanOptions().match("*").build());

                 while (cursor.hasNext()) {
                     String token = new String(cursor.next(), StandardCharsets.UTF_8);
                     byte[] tokenKey = (MAIN_REDIS_KEY + token).getBytes(StandardCharsets.UTF_8);

                     byte[] valueBytes = connection.get(tokenKey);
                     if (valueBytes == null || valueBytes.length == 0) {
                         connection.sRem(setKey, token.getBytes(StandardCharsets.UTF_8));
                         connection.del(tokenKey);
                         continue;
                     }

                     long savedTime = Long.parseLong(new String(valueBytes, StandardCharsets.UTF_8));
                     if (now - savedTime >= expirationMillis) {
                         connection.sRem(MAIN_TOKEN_SET_KEY.getBytes(), token.getBytes(StandardCharsets.UTF_8));
                         connection.del(tokenKey);
                         connection.decr(MAIN_COUNTER_KEY.getBytes());
                     }
                 }
                 return null;
            });
        } catch (Exception e) {
            log.error("만료 토큰 삭제 중 오류 발생", e);
        }
    }

}
