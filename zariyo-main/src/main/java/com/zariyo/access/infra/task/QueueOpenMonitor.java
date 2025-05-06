package com.zariyo.access.infra.task;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class QueueOpenMonitor {

    private final StringRedisTemplate mainRedisTemplate;
    private final StringRedisTemplate eventRedisTemplate;

    private static final String MAIN_COUNTER_KEY = "main:current";
    private static final String MAIN_THRESHOLD_KEY = "main:threshold";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;

    public void startOpenMonitoring() {
        if (scheduledTask != null && !scheduledTask.isDone()) return;

        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            int currentThreshold = Integer.parseInt(
                    Optional.ofNullable(mainRedisTemplate.opsForValue().get(MAIN_THRESHOLD_KEY))
                            .orElse("500"));
            int currentMainUser = Integer.parseInt(
                    Optional.ofNullable(mainRedisTemplate.opsForValue().get(MAIN_COUNTER_KEY))
                            .orElse("0"));
            int open = currentThreshold - currentMainUser;
            if (open > 0) {
                eventRedisTemplate.opsForStream().add("open:count:stream", Map.of("count", String.valueOf(open)));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stopOpenMonitoring() {
        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(true);
        }
    }
}
