package com.zariyo.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static com.zariyo.infra.QueueRedisRepository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final StringRedisTemplate mainRedisTemplate;
    private final StringRedisTemplate queueRedisTemplate;
    private final RedissonClient redissonClient;

    @Scheduled(fixedRate = 1000, initialDelay = 1000)
    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        RLock lock = redissonClient.getLock("queue:scheduler-lock");
        int queueSize = Optional.ofNullable(queueRedisTemplate.opsForList().size(QUEUE_REDIS_KEY)).orElse(0L).intValue();
        if(queueSize > 0){
            try {
                boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
                if (acquired) {
                    openSchedule();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    private void openSchedule() {
        int currentOpenCount = Integer.parseInt(Optional.ofNullable(mainRedisTemplate.opsForValue().get(MAIN_COUNTER_KEY)).orElse("0"));
        int threshold = Integer.parseInt(Optional.ofNullable(mainRedisTemplate.opsForValue().get(MAIN_THRESHOLD_KEY)).orElse("500"));

        if(currentOpenCount < threshold){
            int openCount = threshold - currentOpenCount;
            List<String> tokens = queueRedisTemplate.opsForList().leftPop(QUEUE_REDIS_KEY, openCount);

            if(tokens != null && !tokens.isEmpty()){
                long currentTime = System.currentTimeMillis();
                mainRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (String token : tokens) {
                        byte[] key = (MAIN_REDIS_KEY + token).getBytes();
                        byte[] value = String.valueOf(currentTime).getBytes();
                        connection.set(key, value);
                        connection.sAdd(MAIN_TOKEN_SET_KEY.getBytes(), token.getBytes());
                    }
                    connection.incrBy(MAIN_COUNTER_KEY.getBytes(), tokens.size());
                    return null;
                });
                queueRedisTemplate.opsForValue().increment(QUEUE_EXIT_COUNT, tokens.size());

                int queueSize = Optional.ofNullable(queueRedisTemplate.opsForList().size(QUEUE_REDIS_KEY)).orElse(0L).intValue();
                if(queueSize == 0){
                    queueRedisTemplate.opsForValue().set(QUEUE_PUSH_COUNT, "0");
                    queueRedisTemplate.opsForValue().set(QUEUE_EXIT_COUNT, "0");
                }
            }
        }
    }
}