package com.fifteen.auction.domain.tag.util;

import java.util.List;
import java.util.stream.Collectors;

public class TagGroupKeyUtil {
    public static String toGroupKey(List<String> tags) {
        return tags.stream()
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.joining("_"));
    }
}