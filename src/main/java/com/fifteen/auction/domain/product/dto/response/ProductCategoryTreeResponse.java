package com.fifteen.auction.domain.product.dto.response;

import com.fifteen.auction.domain.product.entity.ProductCategory;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductCategoryTreeResponse {

    private Long id;
    private String name;
    private List<ProductCategoryTreeResponse> children;

    public ProductCategoryTreeResponse(Long id, String name, List<ProductCategoryTreeResponse> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }
}