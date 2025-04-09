package com.fifteen.auction.domain.product.dto.request;

import com.fifteen.auction.domain.product.entity.ProductCategory;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductCategoryTreeResponse {

    private Long id;
    private String name;
    private List<ProductCategoryTreeResponse> children;

    private ProductCategoryTreeResponse(Long id, String name, List<ProductCategoryTreeResponse> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }

    public static ProductCategoryTreeResponse from(ProductCategory category) {
        return new ProductCategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getChildren().stream()
                        .filter(child -> !child.isDeleted())
                        .map(ProductCategoryTreeResponse::from)
                        .collect(Collectors.toList())
        );
    }
}