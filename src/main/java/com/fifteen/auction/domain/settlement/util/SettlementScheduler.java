package com.fifteen.auction.domain.settlement.util;

import com.fifteen.auction.domain.settlement.service.SettlementService;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Async("customExecutor")
    @Scheduled(cron = "0 0 4 * * *")
    public void autoSettleDaily(){
        try {
            String fileurl = settlementService.settleDaily();

            log.info("자동 정산: {}", fileurl);
        }catch (Exception e) {
            // 에러 로그
            log.error("정산 실패: ", e);
            throw new ServerException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }

    @Async("customExecutor")
    @Scheduled(cron = "0 30 3 15 * *")
    public void autoSettleMonthly(){
        try {
            String fileurl = settlementService.settleMonthly();

            log.info("자동 정산: {}", fileurl);
        }catch (Exception e) {
            // 에러 로그
            log.error("정산 실패: ", e);
            throw new ServerException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }
}
