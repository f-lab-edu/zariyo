package com.zariyo.websocket;

import com.zariyo.service.event.QueueStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueHandshakeInterceptor implements HandshakeInterceptor {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isBlank()) {
                token = servletRequest.getServletRequest().getHeader("token");
            }
        }
        if (token == null || token.isBlank()) {
            log.warn("WebSocket 연결 차단(토큰 없음)");
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        log.info("WebSocket 연결 허용 - token={}", token);
        attributes.put("queueToken", token);
        // 10명씩 입장시키는 스케줄러 실행
        eventPublisher.publishEvent(new QueueStartedEvent(token));
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

}
