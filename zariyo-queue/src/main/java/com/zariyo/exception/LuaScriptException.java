package com.zariyo.exception;

import lombok.Getter;

@Getter
public class LuaScriptException extends RuntimeException {

    private final ErrorCode errorCode;

    public LuaScriptException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}