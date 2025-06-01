package com.zariyo.concert.application.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ConcertFileInfo {
    private String fileUrl;
    private String fileType;
    private String originalFileName;
}
