package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상품 단건 조회 (soft delete 고려)
    @Query("SELECT p FROM Product p JOIN FETCH p.images WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // (추후 필요 시 보류)
    // Page<Product> findAllBySellerIdAndDeletedAtIsNull(Long sellerId, Pageable pageable);
    // List<Product> findByCategoryAndDeletedAtIsNull(ProductCategory category);
}