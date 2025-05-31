package com.zariyo.concert.application.service;

import com.zariyo.concert.application.dto.AvailableSeatsDto;
import com.zariyo.concert.application.dto.SeatInfoDto;
import com.zariyo.concert.domain.entity.HallSeat;
import com.zariyo.concert.domain.entity.ScheduleSeat;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleSeatServiceTest {

    @Mock
    private ScheduleSeatRepository scheduleSeatRepository;

    @InjectMocks
    private ScheduleSeatService scheduleSeatService;

    List<ScheduleSeat> mockSeats;

    @BeforeEach
    void setUp() {
        // Mockito 초기화 및 설정이 필요한 경우 여기에 작성
        mockSeats = List.of(
                createScheduleSeat(1L, "VIP", "A", "1", "1", 150000, ScheduleSeat.SeatStatus.AVAILABLE),
                createScheduleSeat(2L, "VIP", "A", "1", "2", 150000, ScheduleSeat.SeatStatus.RESERVED),
                createScheduleSeat(3L, "R", "A", "2", "1", 100000, ScheduleSeat.SeatStatus.AVAILABLE),
                createScheduleSeat(4L, "R", "A", "2", "2", 100000, ScheduleSeat.SeatStatus.RESERVED),
                createScheduleSeat(5L, "S", "B", "1", "1", 80000, ScheduleSeat.SeatStatus.AVAILABLE)
        );
    }

    @Test
    @DisplayName("예약 가능한 좌석 목록을 조회한다")
    void getAvailableSeats_Success() {
        // given
        Long scheduleId = 1L;
        
        given(scheduleSeatRepository.findAvailableSeatsByScheduleId(scheduleId))
                .willReturn(mockSeats);

        // when
        AvailableSeatsDto result = scheduleSeatService.getAvailableSeats(scheduleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(scheduleId);
        assertThat(result.getAvailableSeats()).hasSize(5);
        
        // 첫 번째 좌석 검증
        SeatInfoDto firstSeat = result.getAvailableSeats().get(0);
        assertThat(firstSeat.getScheduleSeatId()).isEqualTo(1L);
        assertThat(firstSeat.getSeatGrade()).isEqualTo("VIP");
        assertThat(firstSeat.getBlock()).isEqualTo("A");
        assertThat(firstSeat.getSeatRow()).isEqualTo("1");
        assertThat(firstSeat.getSeatNumber()).isEqualTo("1");
        assertThat(firstSeat.getPrice()).isEqualTo(new BigDecimal("150000"));
        assertThat(firstSeat.getStatus()).isEqualTo("AVAILABLE");
        
        verify(scheduleSeatRepository, times(1)).findAvailableSeatsByScheduleId(scheduleId);
    }

    @Test
    @DisplayName("전체 좌석 목록을 조회한다 - 예약 가능/불가능 좌석 모두 포함")
    void getAllSeats_Success() {
        // given
        Long scheduleId = 1L;
        
        given(scheduleSeatRepository.findAllSeatsByScheduleId(scheduleId))
                .willReturn(mockSeats);

        // when
        List<SeatInfoDto> result = scheduleSeatService.getAllSeats(scheduleId);

        // then
        assertThat(result).hasSize(5);
        
        // 상태별 좌석 수 검증
        long availableCount = result.stream()
                .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                .count();
        long reservedCount = result.stream()
                .filter(seat -> "RESERVED".equals(seat.getStatus()))
                .count();
        
        assertThat(availableCount).isEqualTo(3);
        assertThat(reservedCount).isEqualTo(2);
        
        // 좌석 등급별 검증
        long vipCount = result.stream()
                .filter(seat -> "VIP".equals(seat.getSeatGrade()))
                .count();
        long rCount = result.stream()
                .filter(seat -> "R".equals(seat.getSeatGrade()))
                .count();
        long sCount = result.stream()
                .filter(seat -> "S".equals(seat.getSeatGrade()))
                .count();
        
        assertThat(vipCount).isEqualTo(2);
        assertThat(rCount).isEqualTo(2);
        assertThat(sCount).isEqualTo(1);
        
        verify(scheduleSeatRepository, times(1)).findAllSeatsByScheduleId(scheduleId);
    }

    @Test
    @DisplayName("예약 가능한 좌석이 없는 경우 빈 목록을 반환한다")
    void getAvailableSeats_EmptyResult() {
        // given
        Long scheduleId = 1L;
        given(scheduleSeatRepository.findAvailableSeatsByScheduleId(scheduleId))
                .willReturn(List.of());

        // when
        AvailableSeatsDto result = scheduleSeatService.getAvailableSeats(scheduleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(scheduleId);
        assertThat(result.getAvailableSeats()).isEmpty();
        
        verify(scheduleSeatRepository, times(1)).findAvailableSeatsByScheduleId(scheduleId);
    }

    @Test
    @DisplayName("좌석 등급별 가격이 올바르게 매핑된다")
    void getAllSeats_CorrectPriceMapping() {
        // given
        Long scheduleId = 1L;
        
        given(scheduleSeatRepository.findAllSeatsByScheduleId(scheduleId))
                .willReturn(mockSeats);

        // when
        List<SeatInfoDto> result = scheduleSeatService.getAllSeats(scheduleId);

        // then
        assertThat(result).hasSize(5);
        
        // VIP 좌석 가격 검증
        SeatInfoDto vipSeat = result.stream()
                .filter(seat -> "VIP".equals(seat.getSeatGrade()))
                .findFirst()
                .orElseThrow();
        assertThat(vipSeat.getPrice()).isEqualTo(new BigDecimal("150000"));
        
        // R석 가격 검증
        SeatInfoDto rSeat = result.stream()
                .filter(seat -> "R".equals(seat.getSeatGrade()))
                .findFirst()
                .orElseThrow();
        assertThat(rSeat.getPrice()).isEqualTo(new BigDecimal("100000"));
        
        // S석 가격 검증
        SeatInfoDto sSeat = result.stream()
                .filter(seat -> "S".equals(seat.getSeatGrade()))
                .findFirst()
                .orElseThrow();
        assertThat(sSeat.getPrice()).isEqualTo(new BigDecimal("80000"));
    }

    // 빌더 패턴을 사용한 Mock 객체 생성 헬퍼 메서드
    private ScheduleSeat createScheduleSeat(Long scheduleSeatId, String seatGrade, 
                                          String block, String seatRow, String seatNumber, 
                                          int price, ScheduleSeat.SeatStatus status) {
        HallSeat hallSeat = HallSeat.builder()
                .block(block)
                .seatRow(seatRow)
                .seatNumber(seatNumber)
                .build();

        return ScheduleSeat.builder()
                .scheduleSeatId(scheduleSeatId)
                .seatGrade(seatGrade)
                .price(BigDecimal.valueOf(price))
                .status(status)
                .hallSeat(hallSeat)
                .build();
    }
}
