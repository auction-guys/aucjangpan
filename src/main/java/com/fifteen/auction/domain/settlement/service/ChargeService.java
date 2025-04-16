package com.fifteen.auction.domain.settlement.service;

import com.fifteen.auction.domain.settlement.entity.Charge;
import com.fifteen.auction.domain.settlement.enums.ChargeType;
import com.fifteen.auction.domain.settlement.repository.ChargeRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeRepository chargeRepository;

    @Transactional
    public void updateCharge(String chargeId, double proportion){

        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND));

        charge.updateProportion(proportion);
    }

    public double getAutoCharge() {
        return chargeRepository.findById(ChargeType.AUTO.toString())
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND))
                .getProportion();
    }

    public double getImmediatelyCharge() {
        return chargeRepository.findById(ChargeType.IMMEDIATELY.toString())
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND))
                .getProportion();
    }
}
