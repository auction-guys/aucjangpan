package com.fifteen.auction.domain.product.dto.response;

public class ProductCategoryResponse {

    private final Long id;
    private final String name;
    private final Long parentId;

    private ProductCategoryResponse(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    public static ProductCategoryResponse of(Long id, String name, Long parentId) {
        return new ProductCategoryResponse(id, name, parentId);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getParentId() { return parentId; }
}