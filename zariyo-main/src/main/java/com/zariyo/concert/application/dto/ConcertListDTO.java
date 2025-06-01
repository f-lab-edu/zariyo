package com.zariyo.concert.application.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ConcertListDTO {
    
    private List<ConcertSummary> concerts;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
