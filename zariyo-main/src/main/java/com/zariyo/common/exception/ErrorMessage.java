package com.zariyo.common.exception;

public record ErrorMessage(
        String code,
        int status,
        String message
) {
    public static ErrorMessage withErrorCode(ErrorCode errorCode) {
        return new ErrorMessage(
                errorCode.name(),
                errorCode.getStatus().value(),
                errorCode.getMessage()
        );
    }
}
