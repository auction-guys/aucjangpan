package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllBySellerId(Long sellerId);
    List<Product> findByCategory(ProductCategory category);
}