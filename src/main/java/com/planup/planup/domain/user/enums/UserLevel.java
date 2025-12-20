package com.planup.planup.domain.user.enums;

import java.util.Arrays;

public enum UserLevel {
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5),
    LEVEL_6(6),
    LEVEL_7(7),
    LEVEL_8(8),
    LEVEL_9(9),
    LEVEL_10(10),
    LEVEL_MAX(Integer.MAX_VALUE);


    private final int value;

    UserLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserLevel fromValue(int value) {
        return Arrays.stream(values())
                .filter(level -> level.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid level: " + value));
    }
}
