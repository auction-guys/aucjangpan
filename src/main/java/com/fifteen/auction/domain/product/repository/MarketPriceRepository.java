package com.fifteen.auction.domain.product.repository;

import com.fifteen.auction.domain.product.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
}
