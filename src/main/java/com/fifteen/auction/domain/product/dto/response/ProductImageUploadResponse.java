package com.fifteen.auction.domain.product.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImageUploadResponse {

    private final List<String> urls;

    public static ProductImageUploadResponse of(List<String> urls) {
        return new ProductImageUploadResponse(urls);
    }
}