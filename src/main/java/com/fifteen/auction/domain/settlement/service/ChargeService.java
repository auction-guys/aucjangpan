package com.fifteen.auction.domain.settlement.service;

import com.fifteen.auction.domain.settlement.entity.Charge;
import com.fifteen.auction.domain.settlement.enums.ChargeType;
import com.fifteen.auction.domain.settlement.repository.ChargeRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeRepository chargeRepository;

    @Transactional
    public void updateCharge(String chargeId, BigDecimal proportion){

        Charge charge = chargeRepository.findById(ChargeType.valueOf(chargeId))
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND));

        charge.updateProportion(proportion);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAutoCharge() {
        return chargeRepository.findById(ChargeType.AUTO)
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND))
                .getProportion();
    }

    @Transactional(readOnly = true)
    public BigDecimal getImmediatelyCharge() {
        return chargeRepository.findById(ChargeType.IMMEDIATELY)
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND))
                .getProportion();
    }
}
