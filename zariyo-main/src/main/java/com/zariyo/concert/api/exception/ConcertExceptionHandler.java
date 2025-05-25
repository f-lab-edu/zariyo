package com.zariyo.concert.api.exception;

import com.zariyo.concert.api.exception.custom.ConcertNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConcertExceptionHandler {

    @ExceptionHandler(ConcertNotFoundException.class)
    public ResponseEntity<String> handleConcertNotFoundException(ConcertNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
