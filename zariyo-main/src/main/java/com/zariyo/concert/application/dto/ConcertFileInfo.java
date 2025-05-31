package com.zariyo.concert.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcertFileInfo {
    private String fileUrl;
    private String fileType;
    private String originalFileName;
}
