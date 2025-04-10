package com.fifteen.auction.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductUpdateRequest {

    @NotBlank
    private final String name;

    private final String description;

    @NotNull
    private final Long categoryId;

    private final String thumbnailUrl;

    private final List<Long> deleteImageIds;

    private final List<String> imageUrls;

    private ProductUpdateRequest(String name, String description, Long categoryId,
                                 String thumbnailUrl, List<Long> deleteImageIds,
                                 List<String> imageUrls) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.thumbnailUrl = thumbnailUrl;
        this.deleteImageIds = deleteImageIds;
        this.imageUrls = imageUrls;
    }

    public static ProductUpdateRequest of(String name, String description, Long categoryId,
                                          String thumbnailUrl, List<Long> deleteImageIds,
                                          List<String> imageUrls) {
        return new ProductUpdateRequest(name, description, categoryId, thumbnailUrl, deleteImageIds, imageUrls);
    }
}