package com.zariyo.concert.application.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcertCacheScheduler {

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshConcertCaches() {
        try {
            evictConcertLocalCaches();
            evictConcertRedisCaches();
        } catch (Exception e) {
            log.error("스케줄러 실패: ", e);
        }
    }

    @CacheEvict(cacheManager = "localCacheManager",value = "concerts", allEntries = true)
    public void evictConcertLocalCaches() {
        log.info("콘서트 캐시 삭제 완료");
    }

    @CacheEvict(cacheManager = "redisCacheManager",value = "concert-detail", allEntries = true)
    public void evictConcertRedisCaches() {
        log.info("콘서트 캐시 삭제 완료");
    }
}
