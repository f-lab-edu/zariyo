package com.zariyo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "이미 대기열에 등록된 사용자입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "대기열에 해당 토큰이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
