package com.zariyo.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private final String status;
    private final String message;
    private final String redirectUrl;

    public static ErrorResponse withRedirect(String status, String message, String redirectUrl) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .redirectUrl(redirectUrl)
                .build();
    }

    public static ErrorResponse of(String status, String message) {
        return ErrorResponse.builder()
                .status(status)
                .message(message)
                .build();
    }
}
