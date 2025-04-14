package com.fifteen.auction.domain.recommend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    TEENS("10-19"),
    TWENTIES("20-29"),
    THIRTIES("30-39"),
    FORTIES("40-49"),
    FIFTIES("50-59");

    private final String label;

    public static AgeGroup from(String value) {
        return switch (value.trim().toUpperCase()) {
            case "10-19", "TEENS" -> TEENS;
            case "20-29", "TWENTIES" -> TWENTIES;
            case "30-39", "THIRTIES" -> THIRTIES;
            case "40-49", "FORTIES" -> FORTIES;
            case "50-59", "FIFTIES" -> FIFTIES;
            default -> throw new IllegalArgumentException("잘못된 연령대 입력: " + value);
        };
    }
}