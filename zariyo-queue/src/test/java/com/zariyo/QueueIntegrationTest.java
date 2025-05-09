package com.zariyo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.api.dto.QueueDto;
import com.zariyo.api.dto.QueueStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
@DisplayName("Queue 통합 테스트")
class QueueIntegrationTest extends TestContainerConfig {

    private static final Logger log = LoggerFactory.getLogger(QueueIntegrationTest.class);
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired StringRedisTemplate mainRedisTemplate;

    private static final int TOTAL_USERS = 1200;
    private static final int MAX_CAPACITY = 500;

    @Test
    @DisplayName("대기열 전체 흐름 테스트")
    void fullQueueFlow() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future<QueueDto>> futures = new ArrayList<>();

        // [Step 1] 사용자 1200명 동시에 /queue/token 요청
        for (int i = 0; i < TOTAL_USERS; i++) {
            futures.add(executor.submit(() -> {
                String response = mvc.perform(get("/queue/token"))
                        .andReturn().getResponse().getContentAsString();
                return om.readValue(response, QueueDto.class);
            }));
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // [Step 2] OPEN / WAITING 분류
        List<QueueDto> openUsers = new ArrayList<>();
        List<QueueDto> waitingUsers = new ArrayList<>();
        for (Future<QueueDto> f : futures) {
            QueueDto user = f.get();
            if (user.getStatus() == QueueStatus.OPEN) openUsers.add(user);
            else waitingUsers.add(user);
        }

        assertThat(openUsers).hasSize(MAX_CAPACITY); // 초기 입장자는 MAX_CAPACITY 명
        assertThat(waitingUsers).hasSize(TOTAL_USERS - MAX_CAPACITY); // 대기자는 나머지

        // [Step 3] 대기열 사용자들 폴링 시작
        ExecutorService pollingExecutor = Executors.newFixedThreadPool(60);
        List<Callable<Void>> pollingTasks = new ArrayList<>();

        waitingUsers.forEach(user -> pollingTasks.add(() -> {
            await().atMost(Duration.ofSeconds(25)).untilAsserted(() -> {
                String pollResponse = mvc.perform(get("/queue/status")
                                .param("token", user.getQueueToken())
                                .param("entryNumber", String.valueOf(user.getEntryNumber())))
                        .andReturn().getResponse().getContentAsString();
                QueueDto pollResult = om.readValue(pollResponse, QueueDto.class);
                assertThat(pollResult.getStatus()).isIn(QueueStatus.OPEN, QueueStatus.WAITING);
            });
            return null;
        }));
        pollingExecutor.invokeAll(pollingTasks);

        // [Step 4] 약 15초 경과 후 대기자 수는 약 700명 이하여야 함
        Thread.sleep(15_000);
        Long tokenSetAfter15 = mainRedisTemplate.opsForSet().size("mainUsers");
        assertThat(tokenSetAfter15)
                .withFailMessage("15초 후에 대기 인원이 700 이하여야 합니다.")
                .isLessThanOrEqualTo(700);

        // [Step 5] 약 25초 경과 후 대기자 수는 약 200명 이하여야 함
        Thread.sleep(15_000); // 총 30초 대기
        Long tokenSetAfter25 = mainRedisTemplate.opsForSet().size("mainUsers");
        System.out.println("tokenSetAfter25 = " + tokenSetAfter25);
        assertThat(tokenSetAfter25)
                .withFailMessage("30초 후에 대기 인원이 200 이하여야 합니다.")
                .isLessThanOrEqualTo(200);
    }
}