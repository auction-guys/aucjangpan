package com.fifteen.auction.domain.product.dto.response;

import java.util.List;

public class ProductImageUploadResponse {

    private final List<String> urls;

    private ProductImageUploadResponse(List<String> urls) {
        this.urls = urls;
    }

    public static ProductImageUploadResponse of(List<String> urls) {
        return new ProductImageUploadResponse(urls);
    }

    public List<String> getUrls() {
        return urls;
    }
}