package com.zariyo.access.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessDto {
    private String token;
    private AccessStatus status;

    public static AccessDto waiting(String token) {
        return AccessDto.builder()
                .token(token)
                .status(AccessStatus.WAITING)
                .build();
    }

    public static AccessDto open(String token) {
        return AccessDto.builder()
                .token(token)
                .status(AccessStatus.OPEN)
                .build();
    }
}
