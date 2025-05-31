package com.zariyo.concert.api.response;

import com.zariyo.concert.application.dto.ConcertDetailDTO;
import com.zariyo.concert.application.dto.ConcertFileInfo;
import com.zariyo.concert.application.dto.ScheduleInfo;
import com.zariyo.concert.application.dto.SeatPriceInfo;
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
public class ConcertDetailResponse {
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
    
    public static ConcertDetailResponse from(ConcertDetailDTO dto) {
        return ConcertDetailResponse.builder()
                .concertId(dto.getConcertId())
                .title(dto.getTitle())
                .categoryName(dto.getCategoryName())
                .hallName(dto.getHallName())
                .ageLimit(dto.getAgeLimit())
                .description(dto.getDescription())
                .runningTime(dto.getRunningTime())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(dto.getStatus())
                .concertFiles(dto.getConcertFiles())
                .schedules(dto.getSchedules())
                .seatPrices(dto.getSeatPrices())
                .build();
    }
}
