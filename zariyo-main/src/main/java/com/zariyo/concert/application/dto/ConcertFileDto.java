package com.zariyo.concert.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConcertFileDto {
    private long fileId;
    private String fileName;
    private String filePath;
    private String fileType;
}
