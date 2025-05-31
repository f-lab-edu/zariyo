package com.zariyo.concert.api.exception.custom;

public class ConcertNotFoundException extends RuntimeException {
    public ConcertNotFoundException(String message) {
        super(message);
    }
}
