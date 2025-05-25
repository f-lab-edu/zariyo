package com.zariyo.concert.application.facade;

import com.zariyo.concert.api.response.ConcertDetailResponse;
import com.zariyo.concert.api.response.ConcertListResponse;
import com.zariyo.concert.application.cache.ConcertCacheManager;
import com.zariyo.concert.application.service.ConcertQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcertFacade {

    private final ConcertQueryService concertQueryService;
    private final ConcertCacheManager cacheManager;

    public ConcertListResponse getConcerts(Pageable pageable, String sortType, Long categoryId) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                SortType.fromValue(sortType)
        );

        String cacheKey = cacheManager.generateCacheKey(categoryId, sortType,
                sortedPageable.getPageNumber(), sortedPageable.getPageSize());

        ConcertListResponse cachedResponse = cacheManager.get(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        ConcertListResponse response = ConcertListResponse.from(concertQueryService.getConcerts(sortedPageable, categoryId));
        cacheManager.put(cacheKey, response);

        return response;
    }

    public ConcertDetailResponse getConcertDetail(Long concertId) {
        String cacheKey = cacheManager.generateDetailCacheKey(concertId);

        ConcertDetailResponse cachedResponse = cacheManager.getDetail(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        ConcertDetailResponse response = ConcertDetailResponse.from(concertQueryService.getConcertDetail(concertId));
        cacheManager.putDetail(cacheKey, response);

        return response;
    }
}
