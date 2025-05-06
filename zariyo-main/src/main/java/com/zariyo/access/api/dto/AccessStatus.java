package com.zariyo.access.api.dto;

public enum AccessStatus {
    WAITING("대기중"),
    OPEN("입장");
    private final String message;
    AccessStatus(String message) { this.message = message; }
}
