package com.fifteen.auction.domain.settlement.service;

import com.fifteen.auction.domain.settlement.entity.Charge;
import com.fifteen.auction.domain.settlement.enums.ChargeType;
import com.fifteen.auction.domain.settlement.repository.ChargeRepository;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateCharge(String chargeId, BigDecimal proportion, Long currentUserId){

        User user = userRepository.findByUserIdAndUserRole(currentUserId)
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_UPDATE_FORBIDDEN));

        Charge charge = chargeRepository.findById(ChargeType.valueOf(chargeId))
                .orElseThrow(() -> new ClientException(ErrorCode.CHARGE_NOT_FOUND));

        charge.updateProportion(proportion);

        log.info("수수료 변경!!  ||  변경 유형 = {}  ||  변경 비율 = {}  ||  담당자 ID = {}", chargeId, proportion, user.getId());
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
