package com.zariyo.concert.application.cache;

import com.zariyo.concert.api.response.ConcertDetailResponse;
import com.zariyo.concert.api.response.ConcertListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertCacheManager {

    private final RedisTemplate<String, Object> mainRedisObjectTemplate;

    private static final String LIST_CACHE_PREFIX = "concert:list:";
    private static final String DETAIL_CACHE_PREFIX = "concert:detail:";
    private static final Duration TTL = Duration.ofHours(24);

    public String generateCacheKey(Long categoryId, String sortType, int page, int size) {
        return String.format("%s%s:%s:%d:%d",
                LIST_CACHE_PREFIX,
                categoryId != null ? categoryId : "all",
                sortType, page, size);
    }

    public String generateDetailCacheKey(Long concertId) {
        return DETAIL_CACHE_PREFIX + concertId;
    }

    public ConcertListResponse get(String key) {
        try {
            Object cached = mainRedisObjectTemplate.opsForValue().get(key);
            if (cached instanceof ConcertListResponse) {
                return (ConcertListResponse) cached;
            }
        } catch (Exception e) {
            log.warn("캐시 조회 실패 - key: {}, error: {}", key, e.getMessage());
        }
        return null;
    }

    public ConcertDetailResponse getDetail(String key) {
        try {
            Object cached = mainRedisObjectTemplate.opsForValue().get(key);
            if (cached instanceof ConcertDetailResponse) {
                return (ConcertDetailResponse) cached;
            }
        } catch (Exception e) {
            log.warn("상세 캐시 조회 실패 - key: {}, error: {}", key, e.getMessage());
        }
        return null;
    }

    public void put(String key, ConcertListResponse response) {
        try {
            mainRedisObjectTemplate.opsForValue().set(key, response, TTL);
        } catch (Exception e) {
            log.warn("캐시 저장 실패 - key: {}, error: {}", key, e.getMessage());
        }
    }

    public void putDetail(String key, ConcertDetailResponse response) {
        try {
            mainRedisObjectTemplate.opsForValue().set(key, response, TTL);
        } catch (Exception e) {
            log.warn("상세 캐시 저장 실패 - key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 매일 00시에 콘서트 캐시를 모두 정리
     */
    @Scheduled(cron = "0 0 0 * * *")
    public int evictAll() {
        int deletedCount = 0;
        try {
            log.info("콘서트 캐시 정리 스케줄러 시작");
            ScanOptions options = ScanOptions.scanOptions()
                    .match("concert:*")
                    .count(50)
                    .build();
            try (Cursor<String> cursor = mainRedisObjectTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    mainRedisObjectTemplate.delete(key);
                    deletedCount++;
                }
            }
        } catch (Exception e) {
            log.error("캐시 삭제 실패 - error: {}", e.getMessage());
        }
        return deletedCount;
    }
}
