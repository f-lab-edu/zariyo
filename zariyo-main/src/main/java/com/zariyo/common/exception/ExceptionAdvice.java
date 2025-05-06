package com.zariyo.common.exception;

import com.zariyo.common.exception.custom.LuaScriptException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(LuaScriptException.class)
    public ResponseEntity<ErrorMessage> handleLuaScriptException(LuaScriptException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(ErrorMessage.withErrorCode(e.getErrorCode()));
    }
}