package com.zariyo.concert.application.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ConcertSummary {
    private Long concertId;
    private String title;
    private String categoryName;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String hallName;
}
