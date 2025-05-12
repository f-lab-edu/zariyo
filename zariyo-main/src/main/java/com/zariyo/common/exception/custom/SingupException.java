package com.zariyo.common.exception.custom;

import com.zariyo.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SingupException  extends RuntimeException {

    private final ErrorCode errorCode;

    public SingupException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
