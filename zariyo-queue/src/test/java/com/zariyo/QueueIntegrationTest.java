package com.zariyo;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.api.dto.QueueStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QueueIntegrationTest extends TestContainerConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("대기열 기본 플로우 테스트")
    public void testBasicQueueFlow() throws Exception {
        int requestCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(requestCount);

        // 1. WAITING 걸리게 요청
        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try {
                    restTemplate.getForEntity(
                            "http://localhost:" + port + "/queue/token",
                            QueueDto.class);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // 2. WAITING 상태의 토큰 발급 시도
        String testToken = null;
        QueueStatus tokenStatus = null;
        for (int i = 0; i < 10; i++) {
            ResponseEntity<QueueDto> response = restTemplate.getForEntity(
                    "http://localhost:" + port + "/queue/token",
                    QueueDto.class);
            QueueDto dto = response.getBody();
            if (dto != null && dto.getStatus() == QueueStatus.WAITING) {
                testToken = dto.getQueueToken();
                tokenStatus = dto.getStatus();
                log.info("WAITING 상태의 토큰 발급: {}, 순번: {}", testToken, dto.getPosition());
                break;
            }
            log.info("토큰 발급 시도 {}: 상태 {}", i + 1, dto != null ? dto.getStatus() : "null");
        }
        assertNotNull(testToken, "WAITING 상태의 토큰이 발급되지 않았습니다.");
        assertEquals(QueueStatus.WAITING, tokenStatus);

        // 3. 웹소켓 연결
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String wsUrl = String.format("ws://localhost:%d/ws/queue?token=%s", port, testToken);
        StompSession session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected(), "웹소켓 연결이 실패했습니다.");

        // 4. 구독 설정
        CompletableFuture<QueueDto> messageFuture = new CompletableFuture<>();
        session.subscribe("/sub/queue/" + testToken, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return QueueDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                QueueDto dto = (QueueDto) payload;
                log.info("메시지 수신: {} / {} / {}", dto.getQueueToken(), dto.getPosition(), dto.getStatus());
                if (dto.getStatus() == QueueStatus.OPEN) {
                    messageFuture.complete(dto);
                }
            }
        });

        // 5. 구독 요청 전송
        session.send("/pub/queue/subscribe", testToken);

        // 6. OPEN 상태 수신 대기
        QueueDto receivedMessage = messageFuture.get();
        assertEquals(QueueStatus.OPEN, receivedMessage.getStatus(), "상태가 OPEN이어야 합니다.");
        assertEquals(testToken, receivedMessage.getQueueToken(), "토큰이 일치해야 합니다.");
        assertEquals(receivedMessage.getPosition(),0, "OPEN 상태일 때는 위치가 0 이여야 합니다");

        // 7. 세션 정리
        session.disconnect();
    }
}

