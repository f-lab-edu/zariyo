package com.zariyo.websocket;

import com.zariyo.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final QueueService queueService;

    /**
     * 사용자가 브라우저를 종료하거나 예기치 않게 WebSocket 연결이 끊긴 경우
     * 세션에서 토큰을 받아서 Redis에서 대기열 정보 제거
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        String token = (String) sessionAttributes.get("queueToken");
        if (token != null) {
            log.info("WebSocket 연결 끊김 - token: {}", token);
            queueService.removeFromQueue(token);
        } else {
            log.warn("연결 끊김 - 세션에 토큰 없음");
        }
    }
}
