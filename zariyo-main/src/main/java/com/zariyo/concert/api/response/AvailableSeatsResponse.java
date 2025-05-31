package com.zariyo.concert.api.response;

import com.zariyo.concert.application.dto.AvailableSeatsDto;
import com.zariyo.concert.application.dto.SeatInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSeatsResponse {
    private Long scheduleId;
    private List<SeatInfoDto> availableSeats;
    private List<SeatInfoDto> allSeats;

    public static AvailableSeatsResponse from(AvailableSeatsDto dto, List<SeatInfoDto> allSeats) {
        return AvailableSeatsResponse.builder()
                .scheduleId(dto.getScheduleId())
                .availableSeats(dto.getAvailableSeats())
                .allSeats(allSeats)
                .build();
    }
}
