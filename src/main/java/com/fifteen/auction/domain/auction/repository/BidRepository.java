package com.fifteen.auction.domain.auction.repository;

import com.fifteen.auction.domain.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
    
}
