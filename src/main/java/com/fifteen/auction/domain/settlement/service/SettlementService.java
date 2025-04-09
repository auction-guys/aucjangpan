package com.fifteen.auction.domain.settlement.service;

import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import com.fifteen.auction.domain.settlement.repository.SettlementRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.ServerException;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    // 정산 - 스케줄러 등록은 crud 끝나고나 고도화 때
    @Transactional
    public void settle() {

        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.PENDING);

        // 정산 가능 데이터 존재 여부
        if(settlements.isEmpty()){
            throw new ServerException(ErrorCode.ORDER_NOT_FOUNDED);
        }

        // csv 파일 저장 위치
        String filePath = System.getProperty("java.io.tmpdir") + "settlement_" + LocalDate.now() + ".csv";

        // csv 파일에 받은 데이터 정리
        List<SettlementResponse> list = settlements.stream()
                .map(settlement -> SettlementResponse.builder()
                .settlementId(settlement.getId().toString())
                .sellerId(settlement.getOrder().getAuction().getProduct().getSeller().getId().toString())
                .orderId(settlement.getOrder().getId().toString())
                .amount(settlement.getOrder().getAuction().getWinPrice().toString())
                .charge(settlement.getCharge().toString())
                .settlementAmount(settlement.getSettlementAmount().toString())
                .settlementDate(LocalDate.now().toString())
                .createdAt(settlement.getCreated_at().toString())
                .bankAccount(settlement.getOrder().getAuction().getProduct().getSeller().getAccountNumber())
                .build()).toList();

        // 정산 처리
        for (Settlement s : settlements) {
            s.settled();
        }

        // 파일 생성
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // CSV 헤더
            writer.writeNext(new String[]{"ID", "Seller ID", "Order Id", "Amount", "Charge", "Settlement Amount", "Settlement Date", "Created At", "Bank Account"});
            // 파일에 데이터 입력
            for (SettlementResponse s : list) {
                writer.writeNext(new String[]{
                        String.valueOf(s.getSettlementId()),
                        String.valueOf(s.getSellerId()),
                        String.valueOf(s.getOrderId()),
                        String.valueOf(s.getAmount()),
                        String.valueOf(s.getCharge()),
                        String.valueOf(s.getSettlementAmount()),
                        String.valueOf(s.getSettlementDate()),
                        String.valueOf(s.getCreatedAt()),
                        String.valueOf(s.getBankAccount())
                });
            }
        } catch (IOException e) {
            throw new ClientException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }
}
