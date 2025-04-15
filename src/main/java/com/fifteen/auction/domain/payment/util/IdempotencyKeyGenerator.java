package com.fifteen.auction.domain.payment.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdempotencyKeyGenerator {

    public String generate(){
        return UUID.randomUUID().toString();
    }
}
