package com.zariyo.concert.application.service;

import com.zariyo.concert.application.dto.*;
import com.zariyo.concert.domain.entity.*;
import com.zariyo.concert.domain.repository.ConcertRepository;
import com.zariyo.concert.domain.repository.ScheduleSeatRepository;
import com.zariyo.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class ConcertCacheTest extends TestContainerConfig {

    @Autowired private ConcertQueryService concertQueryService;
    @Autowired private ScheduleSeatService scheduleSeatService;

    @MockitoBean
    private ConcertRepository concertRepository;
    @MockitoBean
    private ScheduleSeatRepository scheduleSeatRepository;

    private Concert concert;
    private ScheduleSeat scheduleSeat;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .categoryId(5L)
                .categoryName("콘서트")
                .build();

        ConcertHall hall = ConcertHall.builder()
                .hallId(5L)
                .hallName("고척돔")
                .build();

        ConcertFile poster = ConcertFile.builder()
                .fileType(ConcertFile.FileType.POSTER)
                .fileUrl("https://poster.jpg")
                .build();

        SeatPrice seatPrice = SeatPrice.builder()
                .seatGrade("R석")
                .price(BigDecimal.valueOf(100000))
                .build();

        Schedule schedule = Schedule.builder()
                .scheduleId(5L)
                .scheduleDateTime(LocalDate.of(2025, 7, 1).atStartOfDay())
                .build();

        concert = Concert.builder()
                .concertId(5L)
                .title("테스트 콘서트")
                .category(category)
                .concertHall(hall)
                .concertFiles(List.of(poster))
                .seatPrices(List.of(seatPrice))
                .schedules(List.of(schedule))
                .startDate(LocalDate.of(2025, 7, 1))
                .endDate(LocalDate.of(2025, 7, 10))
                .build();

        HallSeat hallSeat = HallSeat.builder()
                .hallSeatId(5L)
                .block("A")
                .seatRow("1")
                .seatNumber("10")
                .build();

        scheduleSeat = ScheduleSeat.builder()
                .scheduleSeatId(150L)
                .seatGrade("VIP")
                .hallSeat(hallSeat)
                .price(new BigDecimal("120000"))
                .status(ScheduleSeat.SeatStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("콘서트 목록 조회 캐싱 테스트")
    void getConcerts_CacheTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Long categoryId = 5L;
        Page<Concert> page = new PageImpl<>(List.of(concert), pageable, 1);

        given(concertRepository.findByCategoryId(categoryId, pageable)).willReturn(page);

        // when - 캐시 미스 & 히트
        ConcertListDTO first = concertQueryService.getConcerts(pageable, categoryId);
        ConcertListDTO second = concertQueryService.getConcerts(pageable, categoryId);

        // then
        verify(concertRepository, times(1)).findByCategoryId(categoryId, pageable);
        assertEquals(first, second);
    }

    @Test
    @DisplayName("콘서트 상세 조회 캐싱 테스트")
    void getConcertDetail_CacheTest() {
        // given
        Long concertId = 5L;
        given(concertRepository.findByIdWithDetails(concertId)).willReturn(Optional.of(concert));

        // when
        ConcertDetailDTO first = concertQueryService.getConcertDetail(concertId);
        ConcertDetailDTO second = concertQueryService.getConcertDetail(concertId);

        // then
        verify(concertRepository, times(1)).findByIdWithDetails(concertId);
        assertEquals(first, second);
    }

    @Test
    @DisplayName("getAvailableSeats 캐싱 테스트")
    void getAvailableSeats_CacheTest() {
        // given
        Long scheduleId = 5L;
        given(scheduleSeatRepository.findAvailableSeatsByScheduleId(scheduleId))
                .willReturn(List.of(scheduleSeat));

        // when
        AvailableSeatsDto first = scheduleSeatService.getAvailableSeats(scheduleId);
        AvailableSeatsDto second = scheduleSeatService.getAvailableSeats(scheduleId);

        // then
        verify(scheduleSeatRepository, times(1)).findAvailableSeatsByScheduleId(scheduleId);
        assertEquals(first, second);
    }

    @Test
    @DisplayName("getAllSeats 캐싱 테스트")
    void getAllSeats_CacheTest() {
        // given
        Long scheduleId = 5L;
        given(scheduleSeatRepository.findAllSeatsByScheduleId(scheduleId))
                .willReturn(List.of(scheduleSeat));

        // when
        List<SeatInfoDto> first = scheduleSeatService.getAllSeats(scheduleId);
        List<SeatInfoDto> second = scheduleSeatService.getAllSeats(scheduleId);

        // then
        verify(scheduleSeatRepository, times(1)).findAllSeatsByScheduleId(scheduleId);
        assertEquals(first, second);
    }
}
