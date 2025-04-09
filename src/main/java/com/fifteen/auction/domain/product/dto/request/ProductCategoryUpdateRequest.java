package com.fifteen.auction.domain.product.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProductCategoryUpdateRequest {
    @NotBlank
    private String name;

    private Long parentId;

    public ProductCategoryUpdateRequest() {}

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }
}