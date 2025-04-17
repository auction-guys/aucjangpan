package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // 상품 단건 조회 (soft delete 고려)
    @Query("SELECT p FROM Product p JOIN FETCH p.images WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    // (추후 필요 시 보류)
    // Page<Product> findAllBySellerIdAndDeletedAtIsNull(Long sellerId, Pageable pageable);
    // List<Product> findByCategoryAndDeletedAtIsNull(ProductCategory category);
//
//    Page<Product> findAllBySellerIdAndDeletedFalse(Long sellerId, Pageable pageable);
//
    List<Product> findByIdNotIn(Collection<Long> ids);
//
//    List<Product> findByCategoryAndDeletedFalse(ProductCategory category);
//
//    Optional<Product> findByIdAndDeletedFalse(Long id);
//
//    Page<Product> findAllByDeletedFalse(Pageable pageable);

    @Query("select p from Product p join fetch p.seller where p.id = :productId and p.deletedAt is null ")
    Optional<Product> findByIdWithSeller(@Param("productId") Long id);


    // 오늘 날짜보다 lastPriceUpdatedAt이 이전인 상품만 조회
    @Query("SELECT p FROM Product p WHERE p.lastPriceUpdatedAt < :today OR p.lastPriceUpdatedAt IS NULL")
    List<Product> findProductsWithoutTodayPrice(@Param("today") LocalDate today);
}