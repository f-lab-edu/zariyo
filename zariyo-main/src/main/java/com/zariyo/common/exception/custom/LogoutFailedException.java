package com.zariyo.common.exception.custom;

import com.zariyo.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class LogoutFailedException extends RuntimeException {
    private final ErrorCode errorCode;

    public LogoutFailedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
