package com.zariyo.concert.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zariyo.concert.application.dto.ReservationStatusDto;
import com.zariyo.concert.application.producer.SeatsOutboxProducer;
import com.zariyo.concert.domain.entity.ScheduleSeat;
import com.zariyo.concert.domain.entity.SeatsOutbox;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import com.zariyo.concert.infra.ReservationStatusRedisRepository;
import com.zariyo.concert.infra.SeatReservationRedisRepository;
import com.zariyo.concert.infra.SeatsOutboxJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private SeatsOutboxJpaRepository seatsOutboxJpaRepository;

    @Mock
    private ScheduleSeatRepository scheduleSeatRepository;

    @Mock
    private SeatReservationRedisRepository seatReservationRedisRepository;

    @Mock
    private ReservationStatusRedisRepository reservationStatusRedisRepository;

    @Mock
    private SeatsOutboxProducer seatsOutboxProducer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private String testReservationToken;
    private List<Long> testSeatIds;

    @BeforeEach
    void setUp() {
        testReservationToken = "test-token-12345";
        testSeatIds = List.of(1L, 2L, 3L);
    }

    @Test
    @DisplayName("예약 요청 성공 시 아웃박스 저장 및 Kafka 전송")
    void shouldSaveOutboxAndSendToKafkaWhenReservationSucceeds() {
        // Given
        when(seatReservationRedisRepository.attemptSeatReservation(any(), anyString())).thenReturn(true);
        
        SeatsOutbox mockSavedOutbox = SeatsOutbox.builder()
                .reservationToken(testReservationToken)
                .seatIds("[1,2,3]")
                .status(SeatsOutbox.OutboxStatus.PENDING)
                .build();

        when(seatsOutboxJpaRepository.save(any(SeatsOutbox.class)))
                .thenReturn(mockSavedOutbox);

        // When
        reservationService.reserveSeats(testSeatIds, testReservationToken);

        // Then: Redis 좌석 예약 시도 검증
        verify(seatReservationRedisRepository, times(1))
                .attemptSeatReservation(testSeatIds, testReservationToken);

        // Then: SeatsOutbox 저장 검증
        ArgumentCaptor<SeatsOutbox> outboxCaptor = ArgumentCaptor.forClass(SeatsOutbox.class);
        verify(seatsOutboxJpaRepository, times(1)).save(outboxCaptor.capture());

        SeatsOutbox savedOutbox = outboxCaptor.getValue();
        assertThat(savedOutbox.getReservationToken()).isEqualTo(testReservationToken);
        assertThat(savedOutbox.getSeatIds()).isEqualTo("[1,2,3]"); // 실제 JSON 형태
        assertThat(savedOutbox.getStatus()).isEqualTo(SeatsOutbox.OutboxStatus.PENDING);
        assertThat(savedOutbox.getCreatedAt()).isNotNull();

        // Then: 성공 상태 Redis 저장 검증
        verify(reservationStatusRedisRepository, times(1))
                .setReservationStatus(testReservationToken, "SUCCESS", "좌석 예약이 완료되었습니다.");

        // Then: SeatsOutboxProducer.sendToKafkaAsync 호출 검증
        verify(seatsOutboxProducer, times(1)).sendToKafkaAsync(mockSavedOutbox);
    }

    @Test
    @DisplayName("Redis 좌석 예약 실패 시 실패 상태 반환")
    void shouldSetFailedStatusWhenReservationFails() {
        // Given: Redis 좌석 예약 실패 설정
        when(seatReservationRedisRepository.attemptSeatReservation(any(), anyString())).thenReturn(false);

        // When
        reservationService.reserveSeats(testSeatIds, testReservationToken);

        // Then: Redis 좌석 예약 시도 검증
        verify(seatReservationRedisRepository, times(1))
                .attemptSeatReservation(testSeatIds, testReservationToken);

        // Then: 실패 상태 Redis 저장 검증
        verify(reservationStatusRedisRepository, times(1))
                .setReservationStatus(testReservationToken, "FAILED", "이미 예약된 좌석이 포함되어 있습니다.");

        // Then: 실패 시에는 아웃박스 저장하지 않음
        verify(seatsOutboxJpaRepository, never()).save(any());
        verify(seatsOutboxProducer, never()).sendToKafkaAsync(any());
    }

    @Test
    @DisplayName("여러 번 호출 시 각각 저장 및 Kafka 전송")
    void shouldHandleMultipleReservationCalls() {
        // Given
        String token1 = "token-1";
        String token2 = "token-2";
        List<Long> seats1 = List.of(1L, 2L);
        List<Long> seats2 = List.of(3L, 4L);

        when(seatReservationRedisRepository.attemptSeatReservation(any(), anyString())).thenReturn(true);

        SeatsOutbox mockOutbox1 = SeatsOutbox.builder()
                .reservationToken(token1)
                .seatIds("[1,2]")
                .status(SeatsOutbox.OutboxStatus.PENDING)
                .build();

        SeatsOutbox mockOutbox2 = SeatsOutbox.builder()
                .reservationToken(token2)
                .seatIds("[3,4]")
                .status(SeatsOutbox.OutboxStatus.PENDING)
                .build();

        when(seatsOutboxJpaRepository.save(any(SeatsOutbox.class)))
                .thenReturn(mockOutbox1)
                .thenReturn(mockOutbox2);

        // When: 두 번 호출
        reservationService.reserveSeats(seats1, token1);
        reservationService.reserveSeats(seats2, token2);

        // Then: 각각 Redis 좌석 예약 시도 검증
        verify(seatReservationRedisRepository, times(1))
                .attemptSeatReservation(seats1, token1);
        verify(seatReservationRedisRepository, times(1))
                .attemptSeatReservation(seats2, token2);

        // Then: 각각 저장 및 Kafka 전송 확인
        verify(seatsOutboxJpaRepository, times(2)).save(any(SeatsOutbox.class));
        verify(seatsOutboxProducer, times(2)).sendToKafkaAsync(any(SeatsOutbox.class));

        // Then: 성공 상태 Redis 저장 검증
        verify(reservationStatusRedisRepository, times(2))
                .setReservationStatus(anyString(), eq("SUCCESS"), eq("좌석 예약이 완료되었습니다."));

        // 호출 순서와 내용 검증
        ArgumentCaptor<SeatsOutbox> outboxCaptor = ArgumentCaptor.forClass(SeatsOutbox.class);
        verify(seatsOutboxJpaRepository, times(2)).save(outboxCaptor.capture());
        
        List<SeatsOutbox> savedOutboxes = outboxCaptor.getAllValues();
        assertThat(savedOutboxes).hasSize(2);
        assertThat(savedOutboxes.get(0).getReservationToken()).isEqualTo(token1);
        assertThat(savedOutboxes.get(1).getReservationToken()).isEqualTo(token2);
    }

    @Test
    @DisplayName("Redis에서 예약 상태 조회")
    void shouldReturnReservationStatus() {
        // Given
        ReservationStatusDto expectedStatus = ReservationStatusDto.builder()
                .status("SUCCESS")
                .message("좌석 예약이 완료되었습니다.")
                .build();

        when(reservationStatusRedisRepository.getReservationStatus(testReservationToken))
                .thenReturn(expectedStatus);

        // When
        ReservationStatusDto actualStatus = reservationService.getReservationStatus(testReservationToken);

        // Then
        verify(reservationStatusRedisRepository, times(1))
                .getReservationStatus(testReservationToken);
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualStatus.getStatus()).isEqualTo("SUCCESS");
        assertThat(actualStatus.getMessage()).isEqualTo("좌석 예약이 완료되었습니다.");
    }

    @Test
    @DisplayName("좌석 상태 업데이트")
    void shouldUpdateSeatStatusSuccessfully() {
        // Given
        List<Long> seatIds = List.of(1L, 2L, 3L);

        ScheduleSeat seat1 = mock(ScheduleSeat.class);
        ScheduleSeat seat2 = mock(ScheduleSeat.class);
        ScheduleSeat seat3 = mock(ScheduleSeat.class);
        List<ScheduleSeat> mockSeats = List.of(seat1, seat2, seat3);

        when(scheduleSeatRepository.findAllById(seatIds)).thenReturn(mockSeats);

        // When
        reservationService.updateSeatHold(seatIds);

        // Then
        verify(scheduleSeatRepository, times(1)).findAllById(seatIds);
        verify(seat1, times(1)).hold();
        verify(seat2, times(1)).hold();
        verify(seat3, times(1)).hold();
    }
}
