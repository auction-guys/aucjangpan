package com.fifteen.auction.domain.product.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final String categoryName;
    private final String thumbnailUrl;
    private final List<String> imageUrls;

    public static ProductResponse of(Long id, String name, String description,
                                     String categoryName, String thumbnailUrl, List<String> imageUrls) {
        return new ProductResponse(id, name, description, categoryName, thumbnailUrl, imageUrls);
    }
}