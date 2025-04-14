package com.fifteen.auction.domain.recommend.enums;

import java.util.Arrays;

public enum Gender {
    MALE, FEMALE;

    public static Gender from(String value) {
        return Arrays.stream(values())
                .filter(g -> g.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 성별 입력: " + value));
    }
}