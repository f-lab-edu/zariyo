package com.zariyo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(QueueException.class)
    public ResponseEntity<ErrorMessage> handleQueueException(QueueException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }

    @ExceptionHandler(LuaScriptException.class)
    public ResponseEntity<ErrorMessage> handleLuaScriptException(LuaScriptException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }
}