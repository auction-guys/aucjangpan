package com.fifteen.auction.domain.auction.repository;

import com.fifteen.auction.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
