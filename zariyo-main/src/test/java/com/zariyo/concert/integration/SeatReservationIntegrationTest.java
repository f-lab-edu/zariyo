package com.zariyo.concert.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.config.TestContainerConfig;
import com.zariyo.concert.api.request.ReservationRequest;
import com.zariyo.concert.domain.entity.SeatsOutbox;
import com.zariyo.concert.infra.SeatsOutboxJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SeatReservationIntegrationTest extends TestContainerConfig {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SeatsOutboxJpaRepository seatsOutboxJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate mainRedisTemplate;

    private String baseUrl;
    private List<Long> testSeatIdList1;
    private List<Long> testSeatIdList2;
    private List<Long> testSeatIdList3;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        testSeatIdList1 = List.of(1L, 2L, 3L);
        testSeatIdList2 = List.of(4L, 5L, 6L);
        testSeatIdList3 = List.of(7L, 8L, 9L);

        String queueToken = "test-queue-token-" + System.currentTimeMillis();
        mainRedisTemplate.opsForValue().set("main:" + queueToken, String.valueOf(System.currentTimeMillis()));

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-QUEUE-TOKEN", queueToken);
    }

    @Test
    @Transactional
    @DisplayName("실제 API 호출로 좌석 예약 전체 플로우 테스트")
    void testSeatReservationApiFlow() throws Exception {
        // Given
        String url = baseUrl + "/api/reservations";
        ReservationRequest request = ReservationRequest.builder()
                .seatIds(testSeatIdList1)
                .build();
        HttpEntity<ReservationRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Then
        String reservationToken = objectMapper.readTree(response.getBody()).get("token").asText();
        assertThat(reservationToken).isNotNull().isNotEmpty();

        // Outbox 확인
        Thread.sleep(5000);
        SeatsOutbox outbox = seatsOutboxJpaRepository.findByReservationToken(reservationToken).orElse(null);
        assertThat(outbox).isNotNull();
        assertThat(outbox.getStatus()).isEqualTo(SeatsOutbox.OutboxStatus.SENT);
    }

    @Test
    @DisplayName("좌석 예약 상태 조회 API 테스트")
    void testReservationStatusApi() throws Exception {
        // Given
        String reservationUrl = baseUrl + "/api/reservations";
        ReservationRequest request = ReservationRequest.builder()
                .seatIds(testSeatIdList2)
                .build();
        
        HttpEntity<ReservationRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> reservationResponse = restTemplate.postForEntity(reservationUrl, entity, String.class);
        
        JsonNode jsonNode = objectMapper.readTree(reservationResponse.getBody());
        String redirectUrl = jsonNode.get("redirectUrl").asText();
        
        // When
        String statusUrl = baseUrl + redirectUrl;
        HttpEntity<Void> statusEntity = new HttpEntity<>(headers);
        ResponseEntity<String> statusResponse = restTemplate.exchange(
                statusUrl, HttpMethod.GET, statusEntity, String.class);

        // Then
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String responseBody = statusResponse.getBody();
        assertThat(responseBody).satisfiesAnyOf(
                body -> assertThat(body).contains("SUCCESS"),
                body -> assertThat(body).contains("FAILED")
        );
    }

    @Test
    @DisplayName("동시 좌석 예약 요청 시 중복 방지 테스트")
    void testConcurrentSeatReservation() throws Exception {
        // Given
        String url = baseUrl + "/api/reservations";
        String firstToken = "first-token-" + System.currentTimeMillis();
        String secondToken = "second-token-" + System.currentTimeMillis();
        
        mainRedisTemplate.opsForValue().set("main:" + firstToken, String.valueOf(System.currentTimeMillis()));
        mainRedisTemplate.opsForValue().set("main:" + secondToken, String.valueOf(System.currentTimeMillis()));
        
        ReservationRequest request = ReservationRequest.builder()
                .seatIds(testSeatIdList3)
                .build();

        HttpHeaders headers1 = new HttpHeaders();
        headers1.setContentType(MediaType.APPLICATION_JSON);
        headers1.set("X-QUEUE-TOKEN", firstToken);
        HttpEntity<ReservationRequest> entity1 = new HttpEntity<>(request, headers1);

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        headers2.set("X-QUEUE-TOKEN", secondToken);
        HttpEntity<ReservationRequest> entity2 = new HttpEntity<>(request, headers2);

        // When
        ResponseEntity<String> response1 = restTemplate.postForEntity(url, entity1, String.class);
        ResponseEntity<String> response2 = restTemplate.postForEntity(url, entity2, String.class);

        String reservationToken1 = objectMapper.readTree(response1.getBody()).get("token").asText();
        String reservationToken2 = objectMapper.readTree(response2.getBody()).get("token").asText();

        // 상태 조회
        String statusUrl1 = baseUrl + "/api/reservations/status?token=" + reservationToken1;
        String statusUrl2 = baseUrl + "/api/reservations/status?token=" + reservationToken2;
        
        ResponseEntity<String> statusResponse1 = restTemplate.exchange(
                statusUrl1, HttpMethod.GET, new HttpEntity<>(headers1), String.class);
        ResponseEntity<String> statusResponse2 = restTemplate.exchange(
                statusUrl2, HttpMethod.GET, new HttpEntity<>(headers2), String.class);
        
        // Then
        boolean firstSuccess = "SUCCESS".equals(objectMapper.readTree(statusResponse1.getBody()).get("status").asText());
        boolean secondSuccess = "SUCCESS".equals(objectMapper.readTree(statusResponse2.getBody()).get("status").asText());
        
        // 둘 중 하나만 성공
        assertThat(firstSuccess ^ secondSuccess).isTrue();

        // 성공한 요청에 대해서만 Outbox 확인
        String successReservationToken = firstSuccess ? reservationToken1 : reservationToken2;
        
        Thread.sleep(2000);
        SeatsOutbox outbox = seatsOutboxJpaRepository.findByReservationToken(successReservationToken).orElse(null);
        assertThat(outbox).isNotNull();
        assertThat(outbox.getStatus()).isEqualTo(SeatsOutbox.OutboxStatus.SENT);
    }
}
