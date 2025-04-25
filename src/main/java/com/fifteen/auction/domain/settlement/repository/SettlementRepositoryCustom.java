package com.fifteen.auction.domain.settlement.repository;

import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettlementRepositoryCustom {

    List<Settlement> findAllByStatus(SettlementStatus settlementStatus);

    Page<SettlementResponse> findBySellerId(Long currentUserId, Pageable pageable);
}
