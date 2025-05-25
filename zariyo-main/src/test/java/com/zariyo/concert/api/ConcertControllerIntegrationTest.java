package com.zariyo.concert.api;

import com.zariyo.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConcertControllerIntegrationTest extends TestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate mainRedisTemplate;

    private static final String QUEUE_TOKEN = "queue-token";

    @BeforeEach
    void setUp() {
        mainRedisTemplate.opsForValue().set("main:" + QUEUE_TOKEN, String.valueOf(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 전체 공연")
    void getConcerts_All_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "upcoming")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts").isNotEmpty())
                .andExpect(jsonPath("$.concerts[0].title").exists())
                .andExpect(jsonPath("$.concerts[0].categoryName").value("콘서트"))
                .andExpect(jsonPath("$.concerts[0].hallName").exists())
                .andExpect(jsonPath("$.concerts[0].posterUrl").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 카테고리별")
    void getConcerts_ByCategory_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "upcoming")
                        .param("categoryId", "1")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts").isNotEmpty())
                .andExpect(jsonPath("$.concerts[0].categoryName").value("콘서트"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - sort 파라미터 없을 때 기본값(UPCOMING) 적용")
    void getConcerts_DefaultSort_API_Integration() throws Exception {
        // when & then - sort 파라미터를 지정하지 않으면 기본적으로 upcoming 정렬 적용
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "10")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts").isNotEmpty())
                .andExpect(jsonPath("$.totalElements").value(3))
                // 기본 upcoming 정렬 검증: BTS(5일 후) > 아이유(10일 후) > BLACKPINK(15일 후)
                .andExpect(jsonPath("$.concerts[0].title").value("BTS 월드투어 [Yet To Come]"))
                .andExpect(jsonPath("$.concerts[1].title").value("아이유 콘서트 [The Golden Hour]"))
                .andExpect(jsonPath("$.concerts[2].title").value("BLACKPINK 월드투어 [Born Pink]"));
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 인기순 정렬")
    void getConcerts_PopularSort_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "popular")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts").isNotEmpty())
                .andExpect(jsonPath("$.concerts[0].title").value("BLACKPINK 월드투어 [Born Pink]"))
                .andExpect(jsonPath("$.concerts[1].title").value("BTS 월드투어 [Yet To Come]"))
                .andExpect(jsonPath("$.concerts[2].title").value("아이유 콘서트 [The Golden Hour]"));
    }

    @Test
    @DisplayName("공연 목록 조회 API 통합 테스트 - 최신순 정렬")
    void getConcerts_LatestSort_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "latest")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts").isNotEmpty())
                .andExpect(jsonPath("$.concerts[0].title").value("아이유 콘서트 [The Golden Hour]"))
                .andExpect(jsonPath("$.concerts[1].title").value("BTS 월드투어 [Yet To Come]"))
                .andExpect(jsonPath("$.concerts[2].title").value("BLACKPINK 월드투어 [Born Pink]"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("공연 상세 조회 API 통합 테스트 - 성공")
    void getConcertDetail_Success_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts/1")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concertId").value(1))
                .andExpect(jsonPath("$.title").value("아이유 콘서트 [The Golden Hour]"))
                .andExpect(jsonPath("$.categoryName").value("콘서트"))
                .andExpect(jsonPath("$.hallName").value("올림픽공원 KSPO DOME"))
                .andExpect(jsonPath("$.ageLimit").value("전체관람가"))
                .andExpect(jsonPath("$.description").value("아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트. 새로운 앨범의 감동을 라이브로 만나보세요"))
                .andExpect(jsonPath("$.runningTime").value(150))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                // 연관 데이터 검증
                .andExpect(jsonPath("$.concertFiles").isArray())
                .andExpect(jsonPath("$.concertFiles").isNotEmpty())
                .andExpect(jsonPath("$.concertFiles[0].fileType").value("POSTER"))
                .andExpect(jsonPath("$.schedules").isArray())
                .andExpect(jsonPath("$.schedules").isNotEmpty())
                .andExpect(jsonPath("$.seatPrices").isArray())
                .andExpect(jsonPath("$.seatPrices").isNotEmpty())
                .andExpect(jsonPath("$.seatPrices[?(@.seatGrade == 'VIP')].price").value(220000.00))
                .andExpect(jsonPath("$.seatPrices[?(@.seatGrade == 'R석')].price").value(170000.00))
                .andExpect(jsonPath("$.seatPrices[?(@.seatGrade == 'S석')].price").value(130000.00));
    }

    @Test
    @DisplayName("공연 상세 조회 API 통합 테스트 - 존재하지 않는 공연")
    void getConcertDetail_NotFound_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts/999")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(content().string("콘서트를 찾을 수 없습니다. ID: 999"));
    }

    @Test
    @DisplayName("페이징 API 통합 테스트")
    void getConcerts_Paging_API_Integration() throws Exception {
        // when & then - 첫 번째 페이지
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "upcoming")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts.length()").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));

        // when & then - 두 번째 페이지
        mockMvc.perform(get("/api/concerts")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "upcoming")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts.length()").value(1))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.pageSize").value(2));
    }

    @Test
    @DisplayName("잘못된 정렬 타입 API 통합 테스트 - SortType.fromValue에서 기본값(UPCOMING)으로 처리됨")
    void getConcerts_InvalidSortType_API_Integration() throws Exception {
        // when & then
        mockMvc.perform(get("/api/concerts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "invalid_sort")
                        .header("X-QUEUE-TOKEN", QUEUE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concerts").isArray())
                .andExpect(jsonPath("$.concerts").isNotEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
