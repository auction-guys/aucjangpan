package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.entity.ProductCategory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllBySellerIdAndDeletedFalse(Long sellerId, Pageable pageable);
    List<Product> findByCategoryAndDeletedFalse(ProductCategory category);
    Optional<Product> findByIdAndDeletedFalse(Long id);
    Page<Product> findAllByDeletedFalse(Pageable pageable);
}