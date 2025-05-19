package com.zariyo.concert.api.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConcertFile {
    private long fileId;
    private String fileName;
    private String filePath;
    private String fileType;
}
