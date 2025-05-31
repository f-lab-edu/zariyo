package com.zariyo.concert.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertDetailDTO {
    private Long concertId;
    private String title;
    private String categoryName;
    private String hallName;
    private String ageLimit;
    private String description;
    private Integer runningTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    
    private List<ConcertFileInfo> concertFiles;
    private List<ScheduleInfo> schedules;
    private List<SeatPriceInfo> seatPrices;
}
