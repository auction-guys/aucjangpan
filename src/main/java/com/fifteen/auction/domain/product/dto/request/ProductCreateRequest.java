package com.fifteen.auction.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
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

    public static ProductCreateRequest of(Long categoryId, String name, String description,
                                          List<String> imageUrls, String thumbnailUrl) {
        return new ProductCreateRequest(categoryId, name, description, imageUrls, thumbnailUrl);
    }
}