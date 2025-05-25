package com.zariyo.concert.api.response;

import com.zariyo.concert.application.dto.ConcertListDTO;
import com.zariyo.concert.application.dto.ConcertSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertListResponse {
    
    private List<ConcertSummary> concerts;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    
    public static ConcertListResponse from(ConcertListDTO dto) {
        return ConcertListResponse.builder()
                .concerts(dto.getConcerts())
                .totalPages(dto.getTotalPages())
                .totalElements(dto.getTotalElements())
                .currentPage(dto.getCurrentPage())
                .pageSize(dto.getPageSize())
                .build();
    }
}
