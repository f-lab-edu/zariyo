package com.zariyo.concert.application.service;

import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.ConcertNotFoundException;
import com.zariyo.concert.application.dto.*;
import com.zariyo.concert.domain.entity.*;
import com.zariyo.concert.domain.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleSeatRepository scheduleSeatRepository;

    @Mock
    private SeatPriceRepository seatPriceRepository;

    @Mock
    private ConcertFileRepository concertFileRepository;

    @InjectMocks
    private FindConcertService findConcertService;

    @Test
    @DisplayName("공연 기본 정보를 조회할 수 있다")
    void getConcertBasicInfo() {
        // given
        Long concertId = 1L;
        ConcertHall hall = ConcertHall.builder().address("Seoul").build();
        Concert concert = Concert.builder()
                .concertId(concertId)
                .title("Test Concert")
                .concertHall(hall)
                .build();

        when(concertRepository.findConcertById(concertId)).thenReturn(Optional.of(concert));

        // when
        ConcertDto result = findConcertService.getConcertBasicInfo(concertId);

        // then
        assertThat(result.getConcertId()).isEqualTo(concertId);
        assertThat(result.getTitle()).isEqualTo("Test Concert");
        assertThat(result.getAddress()).isEqualTo("Seoul");
    }

    @Test
    @DisplayName("존재하지 않는 공연 ID로 조회하면 예외가 발생한다")
    void getConcertBasicInfo_notFound() {
        when(concertRepository.findConcertById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findConcertService.getConcertBasicInfo(1L))
                .isInstanceOf(ConcertNotFoundException.class)
                .hasMessageContaining(ErrorCode.CONCERT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("공연 회차 목록을 조회할 수 있다")
    void getSchedules() {
        Long concertId = 1L;
        Schedule schedule = Schedule.builder()
                .scheduleId(100L)
                .scheduleDateTime(LocalDateTime.of(2025, 5, 20, 20, 0))
                .build();

        when(scheduleRepository.findScheduleByConcertId(concertId)).thenReturn(List.of(schedule));

        List<ScheduleDto> result = findConcertService.getSchedules(concertId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduleId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("좌석 등급별 가격을 조회할 수 있다")
    void getSeatPrices() {
        Long concertId = 1L;
        ConcertSeatPrice price = ConcertSeatPrice.builder()
                .seatGrade("VIP")
                .price(new BigDecimal("150000"))
                .build();

        when(seatPriceRepository.findSeatPriceByConcertId(concertId)).thenReturn(List.of(price));

        List<GradePriceDto> result = findConcertService.getSeatPrices(concertId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGrade()).isEqualTo("VIP");
        assertThat(result.get(0).getPrice()).isEqualTo(new BigDecimal("150000"));
    }

    @Test
    @DisplayName("공연 이미지 파일을 조회할 수 있다")
    void getConcertImageFiles() {
        ConcertFile file = ConcertFile.builder()
                .fileId(1L)
                .originFileName("poster.jpg")
                .filePath("/img/poster.jpg")
                .fileType("MAIN")
                .build();

        when(concertFileRepository.findConcertFileByConcertId(1L)).thenReturn(List.of(file));

        List<ConcertFileDto> result = findConcertService.getConcertImageFiles(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("poster.jpg");
    }

    @Test
    @DisplayName("회차 ID로 예매 가능한 좌석을 조회할 수 있다")
    void getAvailableConcertSeats() {
        ScheduleSeatList seat = ScheduleSeatList.builder()
                .scheduleSeatId(10L)
                .block("A")
                .seatNumber(1)
                .seatGrade("VIP")
                .concertSeatPrice(ConcertSeatPrice.builder().price(new BigDecimal("110000")).build())
                .build();

        when(scheduleSeatRepository.findAvailableSeatsByScheduleId(1L)).thenReturn(List.of(seat));

        List<ConcertSeatDto> result = findConcertService.getAvailableConcertSeats(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isEqualTo(new BigDecimal("110000"));
    }

    @Test
    @DisplayName("회차 ID로 회차 정보를 조회할 수 있다")
    void getSchedule() {
        Schedule schedule = Schedule.builder()
                .scheduleId(2L)
                .scheduleDateTime(LocalDateTime.of(2025, 6, 1, 19, 0))
                .build();

        when(scheduleRepository.findScheduleById(2L)).thenReturn(Optional.of(schedule));

        ScheduleDto result = findConcertService.getSchedule(2L);

        assertThat(result.getScheduleId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("좌석 ID 리스트로 좌석 가격 목록을 조회할 수 있다")
    void getReservedSeatsPrice() {
        ScheduleSeatList seat = ScheduleSeatList.builder()
                .scheduleSeatId(10L)
                .block("B")
                .seatNumber(5)
                .seatGrade("R")
                .concertSeatPrice(ConcertSeatPrice.builder().price(new BigDecimal("77000")).build())
                .build();

        when(scheduleSeatRepository.findByScheduleSeatIdIn(List.of(10L))).thenReturn(List.of(seat));

        List<ConcertSeatDto> result = findConcertService.getReservedSeatsPrice(List.of(10L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGrade()).isEqualTo("R");
    }

}