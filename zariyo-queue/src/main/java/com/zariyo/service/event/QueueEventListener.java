package com.zariyo.service.event;

import com.zariyo.exception.LuaScriptException;
import com.zariyo.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventListener {

    private final QueueService queueService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Async
    @EventListener
    public void handleQueueStarted(QueueStartedEvent event) {
        if (queueService.checkSchedulerLock()) {
            try {
                log.info("락 획득 성공: 큐 처리 시작");
                scheduler.scheduleAtFixedRate(() -> {
                    queueService.refreshSchedulerLock();
                    int openCount = queueService.getMainThreshold() - queueService.getCurrentMainUserCount();
                    if (openCount > 0) {
                        int currentQueueSize = queueService.popTokens(openCount);
                        log.info("현재 오픈 수 openCount={}, 큐 크기 currentQueueSize={}", openCount, currentQueueSize);
                        if (currentQueueSize == 0) {
                            log.info("대기열 모두 입장 완료. 스케줄러 종료");
                            queueService.releaseSchedulerLock();
                            scheduler.shutdownNow();
                        }
                    }
                }, 0, 2, TimeUnit.SECONDS);
            }  catch (LuaScriptException e) {
                log.error("LuaScript 에러: {}", e.getMessage());
            }
        }
    }
}