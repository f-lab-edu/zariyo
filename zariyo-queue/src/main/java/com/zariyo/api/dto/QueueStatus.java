package com.zariyo.api.dto;

import lombok.Getter;

@Getter
public enum QueueStatus {
    WAITING("대기중");
    private final String message;
    QueueStatus(String message) { this.message = message; }
}