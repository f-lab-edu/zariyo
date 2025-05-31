package com.zariyo.concert.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertListDTO {
    
    private List<ConcertSummary> concerts;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
