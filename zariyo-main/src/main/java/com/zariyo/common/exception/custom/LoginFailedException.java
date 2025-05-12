package com.zariyo.common.exception.custom;

import com.zariyo.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class LoginFailedException extends RuntimeException {
    private final ErrorCode errorCode;

    public LoginFailedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
