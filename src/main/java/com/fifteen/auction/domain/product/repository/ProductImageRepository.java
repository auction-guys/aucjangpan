package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findAllByProductId(Long productId);
    Optional<ProductImage> findByProductIdAndIsThumbnailTrue(Long productId);
}