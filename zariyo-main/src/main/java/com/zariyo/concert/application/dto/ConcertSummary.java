package com.zariyo.concert.application.dto;

import com.zariyo.concert.domain.entity.Concert;
import com.zariyo.concert.domain.entity.ConcertFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertSummary {
    private Long concertId;
    private String title;
    private String categoryName;
    private String posterUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String hallName;

    public static ConcertSummary convertToSummary(Concert concert) {
        String posterUrl = concert.getConcertFiles().stream()
                .filter(file -> file.getFileType() == com.zariyo.concert.domain.entity.ConcertFile.FileType.POSTER)
                .findFirst()
                .map(ConcertFile::getFileUrl)
                .orElse(null);

        return ConcertSummary.builder()
                .concertId(concert.getConcertId())
                .title(concert.getTitle())
                .categoryName(concert.getCategory().getCategoryName())
                .posterUrl(posterUrl)
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .hallName(concert.getConcertHall().getHallName())
                .build();
    }
}
