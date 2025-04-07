package com.fifteen.auction.domain.auction.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Random;

@Component
public class AuctionSeqGenerator {
    private final Random random = new Random();

    private static final String CANDIDATE_STRING = "123456789abcdefghklmnpqrstwxyzABCDEFGHKLMNPQRSTWXYZ";
    private static final int RAND_LENGTH = 4;

    public String generate(LocalDate date) {
        String yyMM = date.format(DateTimeFormatter.ofPattern("yyMM"));
        return yyMM + getRandomString();
    }

    private String getRandomString() {
        int candLength = CANDIDATE_STRING.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < RAND_LENGTH; i++) {
            int currentIdx = random.nextInt(candLength);
            sb.append(CANDIDATE_STRING.charAt(currentIdx));
        }

        return sb.toString();
    }
}
