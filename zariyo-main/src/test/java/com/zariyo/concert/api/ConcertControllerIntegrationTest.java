package com.zariyo.concert.api;

import com.zariyo.concert.api.response.AvailableSeatsResponse;
import com.zariyo.concert.api.response.ConcertDetailResponse;
import com.zariyo.concert.api.response.ConcertListResponse;
import com.zariyo.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ConcertControllerIntegrationTest extends TestContainerConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate mainRedisTemplate;

    private static final String QUEUE_TOKEN = "queue-token";
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        mainRedisTemplate.opsForValue().set("main:" + QUEUE_TOKEN, String.valueOf(System.currentTimeMillis()));
        headers = new HttpHeaders();
        headers.set("X-QUEUE-TOKEN", QUEUE_TOKEN);
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 전체 공연")
    void getConcerts_All_API_Integration() {
        // when
        ResponseEntity<ConcertListResponse> response = restTemplate.exchange(
                "/api/concerts?page=0&size=10&sort=upcoming",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConcerts()).isNotEmpty();
        assertThat(response.getBody().getConcerts().get(0).getTitle()).isNotNull();
        assertThat(response.getBody().getConcerts().get(0).getCategoryName()).isEqualTo("콘서트");
        assertThat(response.getBody().getCurrentPage()).isEqualTo(0);
        assertThat(response.getBody().getPageSize()).isEqualTo(10);
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 카테고리별")
    void getConcerts_ByCategory_API_Integration() {
        // when
        ResponseEntity<ConcertListResponse> response = restTemplate.exchange(
                "/api/concerts?page=0&size=10&sort=upcoming&categoryId=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConcerts()).isNotEmpty();
        assertThat(response.getBody().getConcerts().get(0).getCategoryName()).isEqualTo("콘서트");
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - sort 파라미터 없을 때 기본값(UPCOMING) 적용")
    void getConcerts_DefaultSort_API_Integration() {
        // when
        ResponseEntity<ConcertListResponse> response = restTemplate.exchange(
                "/api/concerts?page=0&size=10",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConcerts()).isNotEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
        // 기본 upcoming 정렬 검증: BTS(5일 후) > 아이유(10일 후) > BLACKPINK(15일 후)
        assertThat(response.getBody().getConcerts().get(0).getTitle()).isEqualTo("BTS 월드투어 [Yet To Come]");
        assertThat(response.getBody().getConcerts().get(1).getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(response.getBody().getConcerts().get(2).getTitle()).isEqualTo("BLACKPINK 월드투어 [Born Pink]");
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 인기순 정렬")
    void getConcerts_PopularSort_API_Integration() {
        // when
        ResponseEntity<ConcertListResponse> response = restTemplate.exchange(
                "/api/concerts?page=0&size=10&sort=popular",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConcerts()).isNotEmpty();
        assertThat(response.getBody().getConcerts().get(0).getTitle()).isEqualTo("BLACKPINK 월드투어 [Born Pink]");
        assertThat(response.getBody().getConcerts().get(1).getTitle()).isEqualTo("BTS 월드투어 [Yet To Come]");
        assertThat(response.getBody().getConcerts().get(2).getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 최신순 정렬")
    void getConcerts_LatestSort_API_Integration() {
        // when
        ResponseEntity<ConcertListResponse> response = restTemplate.exchange(
                "/api/concerts?page=0&size=10&sort=latest",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConcerts()).isNotEmpty();
        assertThat(response.getBody().getConcerts().get(0).getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(response.getBody().getConcerts().get(1).getTitle()).isEqualTo("BTS 월드투어 [Yet To Come]");
        assertThat(response.getBody().getConcerts().get(2).getTitle()).isEqualTo("BLACKPINK 월드투어 [Born Pink]");
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("공연 상세 조회 API 통합 테스트 - 성공")
    void getConcertDetail_Success_API_Integration() {
        // when
        ResponseEntity<ConcertDetailResponse> response = restTemplate.exchange(
                "/api/concerts/1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertDetailResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        ConcertDetailResponse concert = response.getBody();
        assertThat(concert.getConcertId()).isEqualTo(1);
        assertThat(concert.getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(concert.getCategoryName()).isEqualTo("콘서트");
        assertThat(concert.getHallName()).isEqualTo("올림픽공원 KSPO DOME");
        assertThat(concert.getAgeLimit()).isEqualTo("전체관람가");
        assertThat(concert.getDescription()).isEqualTo("아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트. 새로운 앨범의 감동을 라이브로 만나보세요");
        assertThat(concert.getRunningTime()).isEqualTo(150);
        assertThat(concert.getStatus()).isEqualTo("ACTIVE");
        
        // 연관 데이터 검증
        assertThat(concert.getConcertFiles()).isNotEmpty();
        assertThat(concert.getConcertFiles().get(0).getFileType()).isEqualTo("POSTER");
        assertThat(concert.getSchedules()).isNotEmpty();
        assertThat(concert.getSeatPrices()).isNotEmpty();
        
        // 좌석 가격 검증
        assertThat(concert.getSeatPrices())
                .extracting("seatGrade", "price")
                .contains(
                    tuple("VIP", new BigDecimal("220000.00")),
                    tuple("R석", new BigDecimal("170000.00")),
                    tuple("S석", new BigDecimal("130000.00"))
                );
    }

    @Test
    @DisplayName("공연 상세 조회 API 통합 테스트 - 존재하지 않는 공연")
    void getConcertDetail_NotFound_API_Integration() {
        // when
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/concerts/999",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("콘서트를 찾을 수 없습니다. ID: 999");
    }

    @Test
    @DisplayName("페이징 API 통합 테스트")
    void getConcerts_Paging_API_Integration() {
        // when - 첫 번째 페이지
        ResponseEntity<ConcertListResponse> firstPageResponse = restTemplate.exchange(
                "/api/concerts?page=0&size=2&sort=upcoming",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then - 첫 번째 페이지
        assertThat(firstPageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstPageResponse.getBody()).isNotNull();
        assertThat(firstPageResponse.getBody().getConcerts()).hasSize(2);
        assertThat(firstPageResponse.getBody().getCurrentPage()).isEqualTo(0);
        assertThat(firstPageResponse.getBody().getPageSize()).isEqualTo(2);
        assertThat(firstPageResponse.getBody().getTotalElements()).isEqualTo(3);
        assertThat(firstPageResponse.getBody().getTotalPages()).isEqualTo(2);

        // when - 두 번째 페이지
        ResponseEntity<ConcertListResponse> secondPageResponse = restTemplate.exchange(
                "/api/concerts?page=1&size=2&sort=upcoming",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then - 두 번째 페이지
        assertThat(secondPageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondPageResponse.getBody()).isNotNull();
        assertThat(secondPageResponse.getBody().getConcerts()).hasSize(1);
        assertThat(secondPageResponse.getBody().getCurrentPage()).isEqualTo(1);
        assertThat(secondPageResponse.getBody().getPageSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("잘못된 정렬 타입 API 통합 테스트 - SortType.fromValue에서 기본값(UPCOMING)으로 처리됨")
    void getConcerts_InvalidSortType_API_Integration() {
        // when
        ResponseEntity<ConcertListResponse> response = restTemplate.exchange(
                "/api/concerts?page=0&size=10&sort=invalid_sort",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ConcertListResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getConcerts()).isNotEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("좌석 조회 API 통합 테스트 - 성공")
    void getAvailableSeats_Success_API_Integration() {
        // when
        ResponseEntity<AvailableSeatsResponse> response = restTemplate.exchange(
                "/api/concerts/schedules/1/seats",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AvailableSeatsResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        AvailableSeatsResponse seatsResponse = response.getBody();
        assertThat(seatsResponse.getScheduleId()).isEqualTo(1);
        assertThat(seatsResponse.getAvailableSeats()).isNotEmpty();
        
        // 첫 번째 좌석 정보 검증
        assertThat(seatsResponse.getAvailableSeats().get(0).getSeatGrade()).isNotNull();
        assertThat(seatsResponse.getAvailableSeats().get(0).getBlock()).isNotNull();
        assertThat(seatsResponse.getAvailableSeats().get(0).getSeatRow()).isNotNull();
        assertThat(seatsResponse.getAvailableSeats().get(0).getSeatNumber()).isNotNull();
        assertThat(seatsResponse.getAvailableSeats().get(0).getPrice()).isNotNull();
        
        // 좌석 등급별 검증
        assertThat(seatsResponse.getAvailableSeats())
                .extracting("seatGrade")
                .contains("VIP", "R석", "S석");
    }

    @Test
    @DisplayName("좌석 조회 API 통합 테스트 - 다양한 스케줄의 좌석 조회")
    void getAvailableSeats_MultipleSchedules_API_Integration() {
        // when & then - 첫 번째 스케줄 좌석 조회
        ResponseEntity<AvailableSeatsResponse> response1 = restTemplate.exchange(
                "/api/concerts/schedules/1/seats",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AvailableSeatsResponse.class
        );

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).isNotNull();
        assertThat(response1.getBody().getScheduleId()).isEqualTo(1);
        assertThat(response1.getBody().getAllSeats()).isNotEmpty();
        assertThat(response1.getBody().getAvailableSeats()).isNotEmpty();

        // when & then - 두 번째 스케줄 좌석 조회
        ResponseEntity<AvailableSeatsResponse> response2 = restTemplate.exchange(
                "/api/concerts/schedules/2/seats",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AvailableSeatsResponse.class
        );

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getScheduleId()).isEqualTo(2);
        assertThat(response2.getBody().getAllSeats()).isNotEmpty();
        assertThat(response2.getBody().getAvailableSeats()).isNotEmpty();

        // when & then - 세 번째 스케줄 좌석 조회
        ResponseEntity<AvailableSeatsResponse> response3 = restTemplate.exchange(
                "/api/concerts/schedules/3/seats",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                AvailableSeatsResponse.class
        );

        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getBody()).isNotNull();
        assertThat(response3.getBody().getScheduleId()).isEqualTo(3);
        assertThat(response3.getBody().getAllSeats()).isNotEmpty();
        assertThat(response3.getBody().getAvailableSeats()).isNotEmpty();
    }
}
