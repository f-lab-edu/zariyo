package com.zariyo.concert.application.service;

import com.zariyo.concert.api.exception.custom.ConcertNotFoundException;
import com.zariyo.concert.application.dto.*;
import com.zariyo.concert.domain.entity.Concert;
import com.zariyo.concert.domain.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertQueryService {

    private final ConcertRepository concertRepository;

    @Transactional(readOnly = true)
    public ConcertListDTO getConcerts(Pageable pageable, Long categoryId) {
        Page<Concert> concertPage = categoryId != null
                ? concertRepository.findByCategoryId(categoryId, pageable)
                : concertRepository.findAll(pageable);

        List<ConcertSummary> concerts = concertPage.getContent().stream()
                .map(ConcertSummary::convertToSummary)
                .collect(Collectors.toList());

        return ConcertListDTO.builder()
                .concerts(concerts)
                .totalPages(concertPage.getTotalPages())
                .totalElements(concertPage.getTotalElements())
                .currentPage(concertPage.getNumber())
                .pageSize(concertPage.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public ConcertDetailDTO getConcertDetail(Long concertId) {
        Concert concert = concertRepository.findByIdWithDetails(concertId)
                .orElseThrow(() -> new ConcertNotFoundException("콘서트를 찾을 수 없습니다. ID: " + concertId));

        List<ConcertFileInfo> fileInfos = concert.getConcertFiles().stream()
                .map(file -> ConcertFileInfo.builder()
                        .fileUrl(file.getFileUrl())
                        .fileType(file.getFileType().name())
                        .originalFileName(file.getOriginalFileName())
                        .build())
                .collect(Collectors.toList());

        List<ScheduleInfo> scheduleInfos = concert.getSchedules().stream()
                .map(schedule -> ScheduleInfo.builder()
                        .scheduleId(schedule.getScheduleId())
                        .scheduleDateTime(schedule.getScheduleDateTime())
                        .build())
                .collect(Collectors.toList());

        List<SeatPriceInfo> priceInfos = concert.getSeatPrices().stream()
                .map(seatPrice -> SeatPriceInfo.builder()
                        .seatGrade(seatPrice.getSeatGrade())
                        .price(seatPrice.getPrice())
                        .build())
                .collect(Collectors.toList());

        return ConcertDetailDTO.builder()
                .concertId(concert.getConcertId())
                .title(concert.getTitle())
                .categoryName(concert.getCategory().getCategoryName())
                .hallName(concert.getConcertHall().getHallName())
                .ageLimit(concert.getAgeLimit())
                .description(concert.getDescription())
                .runningTime(concert.getRunningTime())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .status(concert.getStatus().name())
                .concertFiles(fileInfos)
                .schedules(scheduleInfos)
                .seatPrices(priceInfos)
                .build();
    }
}
