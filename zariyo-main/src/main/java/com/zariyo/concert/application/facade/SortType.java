package com.zariyo.concert.application.facade;

import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

@Getter
public enum SortType {
    UPCOMING("upcoming", Sort.by(Sort.Direction.ASC, "startDate")),
    POPULAR("popular", Sort.by(Sort.Direction.DESC, "reservationCount")),
    LATEST("latest", Sort.by(Sort.Direction.DESC, "createdAt"));

    private final String value;
    private final Sort sort;

    SortType(String value, Sort sort) {
        this.value = value;
        this.sort = sort;
    }

    public static Sort fromValue(String value) {
        return Arrays.stream(SortType.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElse(SortType.UPCOMING)
                .getSort();
    }
}
