package com.fifteen.auction.domain.product.dto.response;

import lombok.Getter;
import java.util.List;

@Getter
public class ProductResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final String categoryName;
    private final String thumbnailUrl;
    private final List<String> imageUrls;

    private ProductResponse(Long id, String name, String description, String categoryName,
                            String thumbnailUrl, List<String> imageUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrls = imageUrls;
    }

    public static ProductResponse of(Long id, String name, String description,
                                     String categoryName, String thumbnailUrl, List<String> imageUrls) {
        return new ProductResponse(id, name, description, categoryName, thumbnailUrl, imageUrls);
    }
}