package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.MarketPrice;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    // 상품의 특정 날짜 시세가 이미 존재하는지 확인
    boolean existsByProductIdAndPriceDate(Long productId, LocalDate marketDate);

    Optional<MarketPrice> findFirstByProductIdAndPriceDate(Long productId, LocalDate priceDate);

    @Query("""
    SELECT mp FROM MarketPrice mp
    JOIN mp.product p
    WHERE p.name = :productName AND mp.priceDate = :priceDate
    ORDER BY p.createdAt DESC
""")
    List<MarketPrice> findByProductNameAndPriceDate(
            @Param("productName") String productName,
            @Param("priceDate") LocalDate priceDate
    );

}

