package com.zariyo.concert.application.facade;

import com.zariyo.concert.api.response.ConcertDetailResponse;
import com.zariyo.concert.api.response.ConcertListResponse;
import com.zariyo.concert.application.cache.ConcertCacheManager;
import com.zariyo.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ConcertFacadeCacheTest extends TestContainerConfig {

    @Autowired
    private ConcertFacade concertFacade;

    @Autowired
    private ConcertCacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> mainRedisObjectTemplate;

    @BeforeEach
    void setUp() {
        cacheManager.evictAll();
    }

    @Test
    @DisplayName("공연 목록 조회 캐싱 통합 테스트 - 첫 번째 호출에서 캐시 저장, 두 번째 호출에서 캐시 히트")
    void getConcerts_CacheIntegration() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String sortType = "upcoming";
        Long categoryId = 1L;
        
        String cacheKey = cacheManager.generateCacheKey(categoryId, sortType, 0, 10);

        // 첫 번째 호출 전에 캐시가 없음을 확인
        Object cachedBefore = mainRedisObjectTemplate.opsForValue().get(cacheKey);
        assertThat(cachedBefore).isNull();

        // when - 첫 번째 호출
        ConcertListResponse firstResult = concertFacade.getConcerts(pageable, sortType, categoryId);

        // then - 첫 번째 호출 검증
        assertThat(firstResult).isNotNull();
        assertThat(firstResult.getConcerts()).isNotEmpty();
        assertThat(firstResult.getTotalElements()).isEqualTo(3);

        // 첫 번째 호출 후 캐시가 저장되었는지 확인
        Object cachedAfterFirst = mainRedisObjectTemplate.opsForValue().get(cacheKey);
        assertThat(cachedAfterFirst).isNotNull();

        // when - 두 번째 호출 (캐시 히트 예상)
        ConcertListResponse secondResult = concertFacade.getConcerts(pageable, sortType, categoryId);

        // then - 두 번째 호출 검증
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.getConcerts()).hasSize(firstResult.getConcerts().size());
        assertThat(secondResult.getTotalElements()).isEqualTo(firstResult.getTotalElements());

        // 두 결과가 동일한 데이터인지 확인 (캐시에서 온 것)
        assertThat(secondResult.getConcerts().get(0).getConcertId())
                .isEqualTo(firstResult.getConcerts().get(0).getConcertId());
    }

    @Test
    @DisplayName("공연 상세 조회 캐싱 통합 테스트 - 캐시 미스 후 캐시 히트")
    void getConcertDetail_CacheIntegration() {
        // given
        Long concertId = 1L; // 아이유 콘서트
        String cacheKey = cacheManager.generateDetailCacheKey(concertId);

        // 첫 번째 호출 전에 캐시가 없음을 확인
        Object cachedBefore = mainRedisObjectTemplate.opsForValue().get(cacheKey);
        assertThat(cachedBefore).isNull();

        // when - 첫 번째 호출 (캐시 미스)
        ConcertDetailResponse firstResult = concertFacade.getConcertDetail(concertId);

        // then - 첫 번째 호출 검증
        assertThat(firstResult).isNotNull();
        assertThat(firstResult.getConcertId()).isEqualTo(concertId);
        assertThat(firstResult.getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
        assertThat(firstResult.getCategoryName()).isEqualTo("콘서트");

        // 첫 번째 호출 후 캐시가 저장되었는지 확인
        Object cachedAfterFirst = mainRedisObjectTemplate.opsForValue().get(cacheKey);
        assertThat(cachedAfterFirst).isNotNull();

        // when - 두 번째 호출 (캐시 히트 예상)
        ConcertDetailResponse secondResult = concertFacade.getConcertDetail(concertId);

        // then - 두 번째 호출 검증
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.getConcertId()).isEqualTo(firstResult.getConcertId());
        assertThat(secondResult.getTitle()).isEqualTo(firstResult.getTitle());
        assertThat(secondResult.getCategoryName()).isEqualTo(firstResult.getCategoryName());
    }

    @Test
    @DisplayName("정렬 타입별 캐시 분리 검증")
    void getConcerts_SortTypeCacheSeparation() {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        Long categoryId = 1L;

        // when - 다른 정렬 타입으로 호출
        ConcertListResponse upcomingResult = concertFacade.getConcerts(pageable, "upcoming", categoryId);
        ConcertListResponse popularResult = concertFacade.getConcerts(pageable, "popular", categoryId);
        ConcertListResponse latestResult = concertFacade.getConcerts(pageable, "latest", categoryId);

        // then - 모든 호출이 성공하고 결과가 있음
        assertThat(upcomingResult).isNotNull();
        assertThat(popularResult).isNotNull();
        assertThat(latestResult).isNotNull();

        assertThat(upcomingResult.getConcerts()).isNotEmpty();
        assertThat(popularResult.getConcerts()).isNotEmpty();
        assertThat(latestResult.getConcerts()).isNotEmpty();

        // 정렬 순서에 맞춰 첫 번째 아이템이 다를 수 있음
        // upcoming: BTS(5일 후) > 아이유(10일 후) > BLACKPINK(15일 후)  
        assertThat(upcomingResult.getConcerts().get(0).getTitle()).isEqualTo("BTS 월드투어 [Yet To Come]");
        
        // popular: BLACKPINK(15000) > BTS(12000) > 아이유(8500)
        assertThat(popularResult.getConcerts().get(0).getTitle()).isEqualTo("BLACKPINK 월드투어 [Born Pink]");
        
        // latest: 아이유(2025-04-16) > BTS(2025-04-14) > BLACKPINK(2025-04-12)
        assertThat(latestResult.getConcerts().get(0).getTitle()).isEqualTo("아이유 콘서트 [The Golden Hour]");
    }

    @Test
    @DisplayName("evictAll - 콘서트 관련 캐시 삭제")
    void evictAll_ShouldDeleteOnlyConcertCache() {
        // given - 콘서트 캐시와 다른 캐시 생성
        String concertListCache = "concert:list:1:upcoming:0:10";
        String concertDetailCache = "concert:detail:1";
        String otherCache = "user:token:12345";
        String anotherCache = "queue:main:67890";

        mainRedisObjectTemplate.opsForValue().set(concertListCache, "concert-list-data");
        mainRedisObjectTemplate.opsForValue().set(concertDetailCache, "concert-detail-data");
        mainRedisObjectTemplate.opsForValue().set(otherCache, "user-data");
        mainRedisObjectTemplate.opsForValue().set(anotherCache, "queue-data");

        // 모든 캐시가 저장되었는지 확인
        assertThat(mainRedisObjectTemplate.opsForValue().get(concertListCache)).isNotNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(concertDetailCache)).isNotNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(otherCache)).isNotNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(anotherCache)).isNotNull();

        // when
        int deletedCount = cacheManager.evictAll();

        // then - 콘서트 캐시만 삭제되고 다른 캐시는 유지
        assertThat(deletedCount).isEqualTo(2); // concert 관련 캐시 2개만 삭제
        assertThat(mainRedisObjectTemplate.opsForValue().get(concertListCache)).isNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(concertDetailCache)).isNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(otherCache)).isNotNull(); // 유지
        assertThat(mainRedisObjectTemplate.opsForValue().get(anotherCache)).isNotNull(); // 유지
    }

    @Test
    @DisplayName("evictAll - 콘서트 캐시 모두 삭제")
    void evictAll_ShouldDeleteAllConcertCachePatterns() {
        // given - 다양한 콘서트 캐시 패턴 생성
        String listCache1 = "concert:list:1:upcoming:0:10";
        String listCache2 = "concert:list:all:popular:1:5";
        String detailCache1 = "concert:detail:1";
        String detailCache2 = "concert:detail:999";

        mainRedisObjectTemplate.opsForValue().set(listCache1, "data1");
        mainRedisObjectTemplate.opsForValue().set(listCache2, "data2");
        mainRedisObjectTemplate.opsForValue().set(detailCache1, "data3");
        mainRedisObjectTemplate.opsForValue().set(detailCache2, "data4");

        // when
        int deletedCount = cacheManager.evictAll();

        // then
        assertThat(deletedCount).isEqualTo(4);
        assertThat(mainRedisObjectTemplate.opsForValue().get(listCache1)).isNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(listCache2)).isNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(detailCache1)).isNull();
        assertThat(mainRedisObjectTemplate.opsForValue().get(detailCache2)).isNull();
    }
}
