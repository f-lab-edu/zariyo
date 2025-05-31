package com.zariyo.concert.api.exception;

import com.zariyo.concert.api.exception.custom.CacheSerializationException;
import com.zariyo.concert.api.exception.custom.ConcertNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ConcertExceptionHandler {

    @ExceptionHandler(ConcertNotFoundException.class)
    public ResponseEntity<String> handleConcertNotFoundException(ConcertNotFoundException ex) {
        log.warn("콘서트를 찾을 수 없음: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CacheSerializationException.class)
    public ResponseEntity<String> handleCacheSerializationException(CacheSerializationException ex) {
        log.error("캐시 직렬화 오류: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
