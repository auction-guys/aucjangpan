package com.fifteen.auction.domain.settlement.repository;

import com.fifteen.auction.domain.settlement.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeRepository extends JpaRepository<Charge, String> {
}
