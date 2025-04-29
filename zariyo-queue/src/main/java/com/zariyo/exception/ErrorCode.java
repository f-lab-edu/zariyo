package com.zariyo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 서버 에러가 발생했습니다."),
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "이미 대기열에 등록된 사용자입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "대기열에 해당 토큰이 존재하지 않습니다."),
    LUA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis Lua 스크립트 실행 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}