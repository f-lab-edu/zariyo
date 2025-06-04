package com.zariyo.concert.application.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class AvailableSeatsDto {
    private Long scheduleId;
    private List<SeatInfoDto> availableSeats;
}
