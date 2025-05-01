package com.zariyo.api.dto;

import lombok.Getter;

@Getter
public enum QueueStatus {
    WAITING("대기중"),
    OPEN("입장");
    private final String message;
    QueueStatus(String message) { this.message = message; }
}