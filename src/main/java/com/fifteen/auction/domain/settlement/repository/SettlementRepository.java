package com.fifteen.auction.domain.settlement.repository;

import com.fifteen.auction.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long>, SettlementRepositoryCustom {

    @Query("select s from Settlement s " +
            "join fetch s.order o " +
            "join fetch o.auction a " +
            "join fetch a.product p " +
            "join fetch p.seller u " +
            "where u.id = :settlementId")
    Optional<Settlement> findByIdSettlementId(@Param("auctionSeq") Long settlementId);
}
