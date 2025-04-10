package com.fifteen.auction.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProductCategoryCreateRequest {

    @NotBlank
    private final String name;

    private final Long parentId;

    private ProductCategoryCreateRequest(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    public static ProductCategoryCreateRequest of(String name, Long parentId) {
        return new ProductCategoryCreateRequest(name, parentId);
    }

    public String getName() { return name; }
    public Long getParentId() { return parentId; }
}
