package com.fifteen.auction.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProductCreateRequest {

    @NotNull
    private final Long categoryId;

    @NotBlank
    private final String name;

    private final String description;

    @NotEmpty
    private final List<String> imageUrls;

    @NotBlank
    private final String thumbnailUrl;

    private ProductCreateRequest(Long categoryId, String name, String description,
                                 List<String> imageUrls, String thumbnailUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ProductCreateRequest of(Long categoryId, String name, String description,
                                          List<String> imageUrls, String thumbnailUrl) {
        return new ProductCreateRequest(categoryId, name, description, imageUrls, thumbnailUrl);
    }

    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}