package com.zariyo.concert.api.exception.custom;

public class CacheSerializationException extends RuntimeException {
    public CacheSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
