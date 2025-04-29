package com.zariyo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.api.dto.QueueDto;
import com.zariyo.api.dto.QueueStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Queue 통합 테스트")
class QueueIntegrationTest extends TestContainerConfig {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired
    StringRedisTemplate mainRedisTemplate;

    private static final int TOTAL_USERS = 1000;
    private static final int MAX_CAPACITY = 500;

    @Test
    @DisplayName("대기열 전체 흐름 테스트")
    void fullQueueFlow() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future<QueueDto>> futures = new ArrayList<>();

        // 1. [Step1] 사용자 1000명 동시에 입장 시도
        for (int i = 0; i < TOTAL_USERS; i++) {
            futures.add(executor.submit(() -> {
                String response = mvc.perform(get("/queue/token"))
                        .andReturn().getResponse().getContentAsString();
                return om.readValue(response, QueueDto.class);
            }));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        List<QueueDto> openUsers = new ArrayList<>();
        List<QueueDto> waitingUsers = new ArrayList<>();
        for (Future<QueueDto> f : futures) {
            QueueDto user = f.get();
            if (user.getStatus() == QueueStatus.OPEN) {
                openUsers.add(user);
            } else {
                waitingUsers.add(user);
            }
        }

        assertThat(openUsers).hasSize(MAX_CAPACITY);
        assertThat(waitingUsers).hasSize(TOTAL_USERS - MAX_CAPACITY);

        // 2. [Step2] 대기열 사용자들 폴링 시작 (입장 가능할 때까지 대기)
        ExecutorService pollingExecutor = Executors.newFixedThreadPool(60);
        List<Callable<Void>> pollingTasks = new ArrayList<>();

        waitingUsers.forEach(user -> pollingTasks.add(() -> {
            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                String pollResponse = mvc.perform(get("/queue/check")
                                .param("token", user.getQueueToken())
                                .param("entryNumber", String.valueOf(user.getEntryNumber())))
                        .andReturn().getResponse().getContentAsString();

                QueueDto pollResult = om.readValue(pollResponse, QueueDto.class);
                assertThat(pollResult.getStatus()).isEqualTo(QueueStatus.OPEN);
            });
            return null;
        }));

        pollingExecutor.invokeAll(pollingTasks);

        // 3. [Step3] TTL 만료 후 main:current 감소 확인
        Thread.sleep(11_000); // 10초 TTL + 1초 버퍼

        String currentCountStr = mainRedisTemplate.opsForValue().get("main:current");
        int currentCount = currentCountStr == null ? 0 : Integer.parseInt(currentCountStr);

        assertThat(currentCount).isLessThanOrEqualTo(MAX_CAPACITY);

        // 4. [Step4] 스케줄러 락이 적절히 회수됐는지 확인 (선택적)
        Boolean lockExists = mainRedisTemplate.hasKey("scheduler-lock");
        assertThat(lockExists).isFalse();
    }
}