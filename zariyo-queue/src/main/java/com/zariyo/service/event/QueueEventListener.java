package com.zariyo.service.event;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventListener {

    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicBoolean broadcasting = new AtomicBoolean(false);

    private final Set<String> openedTokens = ConcurrentHashMap.newKeySet();
    private static final long TOKEN_TTL_SECONDS = 300;

    @EventListener
    public void handleQueueStarted(QueueStartedEvent event) {
        if (!processing.compareAndSet(false, true)) {
            return;
        }
        scheduler.scheduleAtFixedRate(() -> {
            int queueSize = queueService.getQueueSize();
            if (queueSize == 0) {
                log.info("대기열 종료");
                processing.set(false);
                return;
            }
            processNextEntryBatch(queueService.popTopTokens(10));
        }, 2, 1, TimeUnit.SECONDS);
    }

    private void processNextEntryBatch(List<String> tokens) {
        for (String token : tokens) {
            openedTokens.add(token);
            scheduler.schedule(() -> {
                openedTokens.remove(token);
            }, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            QueueDto response = QueueDto.open(token);
            messagingTemplate.convertAndSend("/sub/queue/" + token, response);
        }
        if (broadcasting.compareAndSet(false, true)) {
            CompletableFuture.runAsync(() -> {
                try {
                    queueService.broadcastQueueStatus();
                } finally {
                    broadcasting.set(false);
                }
            });
        }
        if (!tokens.isEmpty()) {
            log.info("{}명 입장 허용 완료: {}", tokens.size(), tokens);
        }
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String token = (String) sha.getSessionAttributes().get("queueToken");
        if (token == null) return;
        String dest = "/sub/queue/" + token;
        if (openedTokens.remove(token)) {
            QueueDto response = QueueDto.open(token);
            messagingTemplate.convertAndSend(dest, response);
            log.info("구독 시점에 OPEN 메시지 재전송: {}", token);
        }
    }
}
