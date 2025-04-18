package com.fifteen.auction.domain.settlement.util;

import com.fifteen.auction.domain.settlement.service.SettlementService;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Scheduled(cron = "0 0 3 * * *")
    public void autoSettle(){
        try {
            String fileurl = settlementService.settle();

            log.info("자동 정산: {}", fileurl);
        }catch (Exception e) {
            // 에러 로그
            log.error("정산 실패: ", e);
            throw new ServerException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }
}
