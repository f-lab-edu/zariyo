package com.zariyo.concert.application.cache;

import com.zariyo.concert.api.response.ConcertDetailResponse;
import com.zariyo.concert.api.response.ConcertListResponse;
import com.zariyo.concert.api.response.AvailableSeatsResponse;
import com.zariyo.concert.application.facade.ConcertFacade;
import com.zariyo.config.TestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConcertFacadeCacheTest extends TestContainerConfig {

    private static final Logger log = Logger.getLogger(ConcertFacadeCacheTest.class.getName());

    @Autowired
    private ConcertFacade concertFacade;

    @Test
    @DisplayName("콘서트 목록 조회 캐싱 테스트 - 첫 번째 호출에서 캐시 저장, 두 번째 호출에서 캐시 히트")
    void getConcerts_CacheTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String sortType = "upcoming";
        Long categoryId = 1L;

        // when - 첫 번째 호출 (캐시 미스)
        long startTime1 = System.currentTimeMillis();
        ConcertListResponse firstResult = concertFacade.getConcerts(pageable, sortType, categoryId);
        long endTime1 = System.currentTimeMillis();
        long firstCallTime = endTime1 - startTime1;

        // then - 첫 번째 호출 검증
        assertThat(firstResult).isNotNull();
        assertThat(firstResult.getConcerts()).isNotEmpty();
        log.info("첫 번째 호출 시간 (DB 조회): " + firstCallTime + "ms");
        // when - 두 번째 호출 (캐시 히트 예상)
        long startTime2 = System.currentTimeMillis();
        ConcertListResponse secondResult = concertFacade.getConcerts(pageable, sortType, categoryId);
        long endTime2 = System.currentTimeMillis();
        long secondCallTime = endTime2 - startTime2;

        // then - 두 번째 호출 검증 (캐시로 인해 빨라야 함)
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.getConcerts()).hasSize(firstResult.getConcerts().size());
        assertThat(secondResult.getTotalElements()).isEqualTo(firstResult.getTotalElements());

        log.info("두 번째 호출 시간 (캐시 히트): " + secondCallTime + "ms");

        // 캐시 히트로 인해 두 번째 호출이 더 빨라야 함
        assertThat(secondCallTime).isLessThan(firstCallTime);

        // Redis 캐시는 직렬화/역직렬화로 인해 새 객체 생성, 내용은 동일
        assertThat(secondResult.getTotalElements()).isEqualTo(firstResult.getTotalElements());
        assertThat(secondResult.getCurrentPage()).isEqualTo(firstResult.getCurrentPage());
    }

    @Test
    @DisplayName("콘서트 상세 조회 캐싱 테스트")
    void getConcertDetail_CacheTest() {
        // given
        Long concertId = 1L;

        // when - 첫 번째 호출 (캐시 미스)
        long startTime1 = System.currentTimeMillis();
        ConcertDetailResponse firstResult = concertFacade.getConcertDetail(concertId);
        long endTime1 = System.currentTimeMillis();

        // then - 첫 번째 호출 검증
        assertThat(firstResult).isNotNull();
        assertThat(firstResult.getConcertId()).isEqualTo(concertId);
        log.info("상세 조회 첫 번째 호출 시간 (DB 조회): " + (endTime1 - startTime1) + "ms");

        // when - 두 번째 호출 (캐시 히트 예상)
        long startTime2 = System.currentTimeMillis();
        ConcertDetailResponse secondResult = concertFacade.getConcertDetail(concertId);
        long endTime2 = System.currentTimeMillis();

        // then - 두 번째 호출 검증 (캐시로 인해 빨라야 함)
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.getConcertId()).isEqualTo(firstResult.getConcertId());
        assertThat(secondResult.getTitle()).isEqualTo(firstResult.getTitle());
        log.info("상세 조회 두 번째 호출 시간 (캐시 히트): " + (endTime2 - startTime2) + "ms");

        assertThat(endTime2 - startTime2).isLessThan(endTime1 - startTime1);
    }

    @Test
    @DisplayName("좌석 조회 캐싱 테스트 - 3초 TTL 확인")
    void getAvailableSeats_CacheTest() throws InterruptedException {
        // given
        Long scheduleId = 1L;

        // when - 첫 번째 호출 (캐시 미스)
        long startTime1 = System.currentTimeMillis();
        AvailableSeatsResponse firstResult = concertFacade.getAvailableSeats(scheduleId);
        long endTime1 = System.currentTimeMillis();

        // then - 첫 번째 호출 검증
        assertThat(firstResult).isNotNull();
        assertThat(firstResult.getScheduleId()).isEqualTo(scheduleId);
        assertThat(firstResult.getAllSeats()).isNotNull(); // 전체 좌석도 포함되어야 함
        assertThat(firstResult.getAvailableSeats()).isNotNull(); // 예약 가능한 좌석
        log.info("좌석 조회 첫 번째 호출 시간 (DB 조회): " + (endTime1 - startTime1) + "ms");

        // when - 두 번째 호출 (3초 내, 캐시 히트 예상)
        long startTime2 = System.currentTimeMillis();
        AvailableSeatsResponse secondResult = concertFacade.getAvailableSeats(scheduleId);
        long endTime2 = System.currentTimeMillis();

        // then - 캐시 히트 검증 (응답 시간으로 확인)
        assertThat(secondResult.getScheduleId()).isEqualTo(firstResult.getScheduleId());
        assertThat(secondResult.getAllSeats()).hasSize(firstResult.getAllSeats().size());
        assertThat(secondResult.getAvailableSeats()).hasSize(firstResult.getAvailableSeats().size());
        assertThat(endTime2 - startTime2).isLessThan(endTime1 - startTime1);
        log.info("좌석 조회 두 번째 호출 시간 (캐시 히트): " + (endTime2 - startTime2) + "ms");

        // when - 3초 대기 후 호출 (available-seats 캐시 만료, all-seats는 유지)
        log.info("4초 대기 중... (available-seats 캐시 TTL 테스트)");
        Thread.sleep(4000);

        long startTime3 = System.currentTimeMillis();
        AvailableSeatsResponse thirdResult = concertFacade.getAvailableSeats(scheduleId);
        long endTime3 = System.currentTimeMillis();

        // then - available-seats는 재조회, all-seats는 캐시 히트
        assertThat(thirdResult).isNotNull();
        assertThat(thirdResult.getScheduleId()).isEqualTo(scheduleId);
        assertThat(thirdResult.getAllSeats()).hasSize(firstResult.getAllSeats().size());
        assertThat(thirdResult.getAvailableSeats()).hasSize(firstResult.getAvailableSeats().size());
        log.info("좌석 조회 세 번째 호출 시간 (available-seats 재조회, all-seats 캐시): " + (endTime3 - startTime3) + "ms");
    }
}
