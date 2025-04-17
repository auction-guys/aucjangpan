package com.fifteen.auction.domain.product.dto.request;


import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class ProductImageUploadRequest {

    @NotEmpty
    private final List<MultipartFile> images;

    public static ProductImageUploadRequest of(List<MultipartFile> images) {
        return new ProductImageUploadRequest(images);
    }
}