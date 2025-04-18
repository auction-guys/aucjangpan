package com.fifteen.auction.domain.settlement.repository;

import com.fifteen.auction.domain.settlement.entity.Charge;
import com.fifteen.auction.domain.settlement.enums.ChargeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChargeRepository extends JpaRepository<Charge, String> {
    Optional<Charge> findById(ChargeType chargeType);
}
