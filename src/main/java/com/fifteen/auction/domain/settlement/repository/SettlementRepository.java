package com.fifteen.auction.domain.settlement.repository;

import com.fifteen.auction.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
