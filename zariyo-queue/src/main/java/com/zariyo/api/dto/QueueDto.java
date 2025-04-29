package com.zariyo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueueDto {
    private String queueToken;
    private int entryNumber;
    private int position;
    private QueueStatus status;

    public static QueueDto waiting(String token, int entryNumber, int position) {
        return QueueDto.builder()
                .queueToken(token)
                .entryNumber(entryNumber)
                .position(position)
                .status(QueueStatus.WAITING)
                .build();
    }

    public static QueueDto open(String token) {
        return QueueDto.builder()
                .queueToken(token)
                .status(QueueStatus.OPEN)
                .build();
    }
}