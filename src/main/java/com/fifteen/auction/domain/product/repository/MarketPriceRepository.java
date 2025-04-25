package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.MarketPrice;
import com.fifteen.auction.domain.product.enums.PriceType;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    // 상품의 특정 날짜 시세가 이미 존재하는지 확인
    boolean existsByProductIdAndPriceDateAndPriceType(Long productId, LocalDate priceDate, PriceType priceType);

    Optional<MarketPrice> findFirstByProductIdAndPriceDateAndPriceType(Long productId, LocalDate priceDate, PriceType priceType);

    @Query("""
    SELECT mp FROM MarketPrice mp
    JOIN mp.product p
    WHERE p.name = :productName AND mp.priceDate = :priceDate AND mp.priceType = :priceType
    ORDER BY p.createdAt DESC
""")
    List<MarketPrice> findByProductNameAndPriceDateAndPriceType(
            @Param("productName") String productName,
            @Param("priceDate") LocalDate priceDate,
            @Param("priceType") PriceType priceType
    );

    // 특정 상품명 + 날짜 리스트 + PriceType 조합으로 다건 조회 (예: 향후 3개월)
    @Query("""
        SELECT mp FROM MarketPrice mp
        JOIN mp.product p
        WHERE p.name = :productName AND mp.priceDate IN :dates AND mp.priceType = :priceType
    """)
    List<MarketPrice> findByProductNameAndPriceDateInAndPriceType(
            @Param("productName") String productName,
            @Param("dates") List<LocalDate> dates,
            @Param("priceType") PriceType priceType
    );

}

