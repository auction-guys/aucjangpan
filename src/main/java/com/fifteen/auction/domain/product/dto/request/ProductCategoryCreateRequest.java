package com.fifteen.auction.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ProductCategoryCreateRequest {

    @NotBlank
    private final String name;

    private final Long parentId;

    public static ProductCategoryCreateRequest of(String name, Long parentId) {
        return new ProductCategoryCreateRequest(name, parentId);
    }
}