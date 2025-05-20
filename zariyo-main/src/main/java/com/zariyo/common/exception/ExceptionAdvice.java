package com.zariyo.common.exception;

import com.zariyo.common.exception.custom.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(SingupException.class)
    public ResponseEntity<ErrorMessage> handleQueueException(SingupException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorMessage> handleLoginFailedException(LoginFailedException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }

    @ExceptionHandler(LogoutFailedException.class)
    public ResponseEntity<ErrorMessage> handleLogoutFailedException(LogoutFailedException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }

    @ExceptionHandler(ConcertNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleConcertNotFoundException(ConcertNotFoundException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }

    @ExceptionHandler(DuplicateSeatReservationException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateSeatReservationException(DuplicateSeatReservationException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }
}
