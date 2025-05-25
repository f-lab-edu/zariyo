package com.zariyo.concert.application.service;

import com.zariyo.concert.api.exception.custom.ConcertNotFoundException;
import com.zariyo.concert.application.dto.*;
import com.zariyo.concert.domain.entity.*;
import com.zariyo.concert.domain.repository.ConcertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcertQueryServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private ConcertQueryService concertQueryService;

    @Test
    @DisplayName("공연 목록 조회 - 전체 공연")
    void getConcerts_All() {
        // given
        Category category = Category.builder()
                .categoryId(1L)
                .categoryName("콘서트")
                .build();
        
        ConcertHall hall = ConcertHall.builder()
                .hallId(1L)
                .hallName("올림픽공원 KSPO DOME")
                .address("서울특별시 송파구 올림픽로 424")
                .build();

        Concert concert = Concert.builder()
                .concertId(1L)
                .title("아이유 콘서트 [The Golden Hour]")
                .category(category)
                .concertHall(hall)
                .ageLimit("전체관람가")
                .description("아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트")
                .runningTime(150)
                .reservationCount(8500)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(40))
                .status(Concert.ConcertStatus.ACTIVE)
                .concertFiles(List.of())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Concert> concertPage = new PageImpl<>(List.of(concert), pageable, 1);

        when(concertRepository.findAll(any(Pageable.class))).thenReturn(concertPage);

        // when
        ConcertListDTO result = concertQueryService.getConcerts(pageable, null);

        // then
        assertThat(result.getConcerts()).hasSize(1);
        assertThat(result.getConcerts().get(0).getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(result.getConcerts().get(0).getCategoryName()).isEqualTo("콘서트");
        assertThat(result.getConcerts().get(0).getHallName()).isEqualTo("올림픽공원 KSPO DOME");
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("공연 목록 조회 - 카테고리별")
    void getConcerts_ByCategory() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder()
                .categoryId(categoryId)
                .categoryName("콘서트")
                .build();
        
        ConcertHall hall = ConcertHall.builder()
                .hallId(1L)
                .hallName("올림픽공원 KSPO DOME")
                .build();

        Concert concert = Concert.builder()
                .concertId(1L)
                .title("아이유 콘서트 [The Golden Hour]")
                .category(category)
                .concertHall(hall)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(40))
                .status(Concert.ConcertStatus.ACTIVE)
                .concertFiles(List.of())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Concert> concertPage = new PageImpl<>(List.of(concert), pageable, 1);

        when(concertRepository.findByCategoryId(eq(categoryId), any(Pageable.class))).thenReturn(concertPage);

        // when
        ConcertListDTO result = concertQueryService.getConcerts(pageable, categoryId);

        // then
        assertThat(result.getConcerts()).hasSize(1);
        assertThat(result.getConcerts().get(0).getCategoryName()).isEqualTo("콘서트");
    }

    @Test
    @DisplayName("공연 상세 조회 - 성공")
    void getConcertDetail_Success() {
        // given
        Long concertId = 1L;
        
        Category category = Category.builder()
                .categoryId(1L)
                .categoryName("콘서트")
                .build();
        
        ConcertHall hall = ConcertHall.builder()
                .hallId(1L)
                .hallName("올림픽공원 KSPO DOME")
                .address("서울특별시 송파구 올림픽로 424")
                .build();

        ConcertFile posterFile = ConcertFile.builder()
                .fileId(1L)
                .originalFileName("iu_golden_hour_poster.jpg")
                .changedFileName("uuid_iu_golden_hour_poster.jpg")
                .fileUrl("https://example.com/files/iu_golden_hour_poster.jpg")
                .fileType(ConcertFile.FileType.POSTER)
                .build();

        Schedule schedule = Schedule.builder()
                .scheduleId(1L)
                .scheduleDateTime(LocalDateTime.now().plusDays(15))
                .build();

        SeatPrice seatPrice = SeatPrice.builder()
                .priceId(1L)
                .seatGrade("VIP")
                .price(new BigDecimal("220000"))
                .build();

        Concert concert = Concert.builder()
                .concertId(concertId)
                .title("아이유 콘서트 [The Golden Hour]")
                .category(category)
                .concertHall(hall)
                .ageLimit("전체관람가")
                .description("아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트")
                .runningTime(150)
                .reservationCount(8500)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(40))
                .status(Concert.ConcertStatus.ACTIVE)
                .concertFiles(List.of(posterFile))
                .schedules(List.of(schedule))
                .seatPrices(List.of(seatPrice))
                .build();

        when(concertRepository.findByIdWithDetails(concertId)).thenReturn(Optional.of(concert));

        // when
        ConcertDetailDTO result = concertQueryService.getConcertDetail(concertId);

        // then
        assertThat(result.getConcertId()).isEqualTo(concertId);
        assertThat(result.getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(result.getCategoryName()).isEqualTo("콘서트");
        assertThat(result.getHallName()).isEqualTo("올림픽공원 KSPO DOME");
        assertThat(result.getAgeLimit()).isEqualTo("전체관람가");
        assertThat(result.getDescription()).isEqualTo("아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트");
        assertThat(result.getRunningTime()).isEqualTo(150);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getConcertFiles()).hasSize(1);
        assertThat(result.getSchedules()).hasSize(1);
        assertThat(result.getSeatPrices()).hasSize(1);
    }

    @Test
    @DisplayName("공연 상세 조회 - 존재하지 않는 공연")
    void getConcertDetail_NotFound() {
        // given
        Long concertId = 999L;
        when(concertRepository.findByIdWithDetails(concertId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> concertQueryService.getConcertDetail(concertId))
                .isInstanceOf(ConcertNotFoundException.class)
                .hasMessage("콘서트를 찾을 수 없습니다. ID: " + concertId);
    }

    @Test
    @DisplayName("Concert 엔티티에서 ConcertSummary 변환 검증")
    void convertToSummary_Conversion() {
        // given
        Category category = Category.builder()
                .categoryId(1L)
                .categoryName("콘서트")
                .build();
        
        ConcertHall hall = ConcertHall.builder()
                .hallId(1L)
                .hallName("올림픽공원 KSPO DOME")
                .build();

        ConcertFile posterFile = ConcertFile.builder()
                .fileUrl("https://example.com/poster.jpg")
                .fileType(ConcertFile.FileType.POSTER)
                .build();

        Concert concert = Concert.builder()
                .concertId(1L)
                .title("아이유 콘서트 [The Golden Hour]")
                .category(category)
                .concertHall(hall)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(40))
                .concertFiles(List.of(posterFile))
                .build();

        // when
        ConcertSummary summary = ConcertSummary.convertToSummary(concert);

        // then
        assertThat(summary.getConcertId()).isEqualTo(1L);
        assertThat(summary.getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(summary.getCategoryName()).isEqualTo("콘서트");
        assertThat(summary.getHallName()).isEqualTo("올림픽공원 KSPO DOME");
        assertThat(summary.getPosterUrl()).isEqualTo("https://example.com/poster.jpg");
        assertThat(summary.getStartDate()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(summary.getEndDate()).isEqualTo(LocalDate.now().plusDays(40));
    }

    @Test
    @DisplayName("콘서트 파일 변환 테스트")
    void convertConcertFiles() {
        // given
        ConcertFile posterFile = ConcertFile.builder()
                .fileId(1L)
                .originalFileName("poster.jpg")
                .changedFileName("uuid_poster.jpg")
                .fileUrl("https://example.com/poster.jpg")
                .fileType(ConcertFile.FileType.POSTER)
                .build();

        ConcertFile descFile = ConcertFile.builder()
                .fileId(2L)
                .originalFileName("description.jpg")
                .changedFileName("uuid_description.jpg")
                .fileUrl("https://example.com/description.jpg")
                .fileType(ConcertFile.FileType.DESCRIPTION)
                .build();

        Concert concert = createMockConcert();
        concert = Concert.builder()
                .concertId(concert.getConcertId())
                .title(concert.getTitle())
                .category(concert.getCategory())
                .concertHall(concert.getConcertHall())
                .ageLimit(concert.getAgeLimit())
                .description(concert.getDescription())
                .runningTime(concert.getRunningTime())
                .reservationCount(concert.getReservationCount())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .status(concert.getStatus())
                .concertFiles(List.of(posterFile, descFile))
                .schedules(List.of())
                .seatPrices(List.of())
                .build();

        when(concertRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concert));

        // when
        ConcertDetailDTO result = concertQueryService.getConcertDetail(1L);

        // then
        assertThat(result.getConcertFiles()).hasSize(2);
        assertThat(result.getConcertFiles()).extracting("fileType")
                .containsExactlyInAnyOrder("POSTER", "DESCRIPTION");
        assertThat(result.getConcertFiles()).extracting("originalFileName")
                .containsExactlyInAnyOrder("poster.jpg", "description.jpg");
    }

    @Test
    @DisplayName("좌석 가격 정보 변환 테스트")
    void convertSeatPrices() {
        // given
        SeatPrice vipPrice = SeatPrice.builder()
                .priceId(1L)
                .seatGrade("VIP")
                .price(new BigDecimal("220000"))
                .build();

        SeatPrice rPrice = SeatPrice.builder()
                .priceId(2L)
                .seatGrade("R석")
                .price(new BigDecimal("170000"))
                .build();

        Concert concert = createMockConcert();
        concert = Concert.builder()
                .concertId(concert.getConcertId())
                .title(concert.getTitle())
                .category(concert.getCategory())
                .concertHall(concert.getConcertHall())
                .ageLimit(concert.getAgeLimit())
                .description(concert.getDescription())
                .runningTime(concert.getRunningTime())
                .reservationCount(concert.getReservationCount())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .status(concert.getStatus())
                .concertFiles(List.of())
                .schedules(List.of())
                .seatPrices(List.of(vipPrice, rPrice))
                .build();

        when(concertRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concert));

        // when
        ConcertDetailDTO result = concertQueryService.getConcertDetail(1L);

        // then
        assertThat(result.getSeatPrices()).hasSize(2);
        assertThat(result.getSeatPrices()).extracting("seatGrade")
                .containsExactlyInAnyOrder("VIP", "R석");
        assertThat(result.getSeatPrices()).extracting("price")
                .containsExactlyInAnyOrder(new BigDecimal("220000"), new BigDecimal("170000"));
    }

    private Concert createMockConcert() {
        Category category = Category.builder()
                .categoryId(1L)
                .categoryName("콘서트")
                .build();
        
        ConcertHall hall = ConcertHall.builder()
                .hallId(1L)
                .hallName("올림픽공원 KSPO DOME")
                .build();

        return Concert.builder()
                .concertId(1L)
                .title("아이유 콘서트 [The Golden Hour]")
                .category(category)
                .concertHall(hall)
                .ageLimit("전체관람가")
                .description("아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트")
                .runningTime(150)
                .reservationCount(8500)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(40))
                .status(Concert.ConcertStatus.ACTIVE)
                .build();
    }
}
