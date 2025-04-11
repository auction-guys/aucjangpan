package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.Product;
import com.fifteen.auction.domain.product.entity.ProductCategory;

import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllBySellerIdAndDeletedFalse(Long sellerId, Pageable pageable);
    List<Product> findByIdNotIn(Collection<Long> ids);
    List<Product> findByCategoryAndDeletedFalse(ProductCategory category);
    Optional<Product> findByIdAndDeletedFalse(Long id);
    Page<Product> findAllByDeletedFalse(Pageable pageable);

    // 오늘 날짜보다 lastPriceUpdatedAt이 이전인 상품만 조회
    @Query("SELECT p FROM Product p WHERE p.lastPriceUpdatedAt < :today OR p.lastPriceUpdatedAt IS NULL")
    List<Product> findProductsWithoutTodayPrice(@Param("today") LocalDate today);
}