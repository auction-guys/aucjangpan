package com.fifteen.auction.domain.product.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ProductImageUploadRequest {

    private final List<MultipartFile> images;

    private ProductImageUploadRequest(List<MultipartFile> images) {
        this.images = images;
    }

    public static ProductImageUploadRequest of(List<MultipartFile> images) {
        return new ProductImageUploadRequest(images);
    }

    public List<MultipartFile> getImages() {
        return images;
    }
}