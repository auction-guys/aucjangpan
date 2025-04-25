package com.fifteen.auction.fixtures;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.entity.ProductCategory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductFixture {
    public static Product ofUser(Long productId, Long userId) {
        Product product = Product.create(
                UserFixture.createDefaultSeller(userId),
                ProductCategory.create("category", null),
                "product",
                "good product, man"
        );
        ReflectionTestUtils.setField(product, "id", productId
        );
        return product;
    }
}
