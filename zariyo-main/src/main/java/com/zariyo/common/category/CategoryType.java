package com.zariyo.common.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CategoryType {

    CONCERT(1L, "콘서트");

    private final long id;
    private final String name;

    CategoryType(long id, String name) {
        this.id = id;
        this.name = name;
    }

}
