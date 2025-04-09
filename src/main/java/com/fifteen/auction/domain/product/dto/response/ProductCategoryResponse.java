package com.fifteen.auction.domain.product.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCategoryResponse {

    private final Long id;
    private final String name;
    private final Long parentId;

    public static ProductCategoryResponse of(Long id, String name, Long parentId) {
        return new ProductCategoryResponse(id, name, parentId);
    }
}