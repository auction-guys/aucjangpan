package com.fifteen.auction.domain.auction.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClockHolder {
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
