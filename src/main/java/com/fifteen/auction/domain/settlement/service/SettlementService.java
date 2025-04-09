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
        if (settlements.isEmpty()) {
            throw new ServerException(ErrorCode.SETTLEMENT_NOT_FOUNDED);
        }

        // csv 파일 저장 위치, 생성
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

        // 파일 작성
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

    @Transactional
    public void settleImmediately(Long settlementId, Long currentUserId) {
        // 검증
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ClientException(ErrorCode.SETTLEMENT_NOT_FOUNDED));
        if (!settlement.getOrder().getAuction().getProduct().getSeller().getId().equals(currentUserId)) {
            throw new ClientException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        // csv 파일 저장 위치, 생성 - 이것도 나중에 환경변수 같은거롤 변경
        String filePath = System.getProperty("java.io.tmpdir") + "settlement_immediately_"+ currentUserId + LocalDate.now() + ".csv";

        // csv 파일에 받은 데이터 정리
        SettlementResponse dto = SettlementResponse.builder()
                        .settlementId(settlement.getId().toString())
                        .sellerId(settlement.getOrder().getAuction().getProduct().getSeller().getId().toString())
                        .orderId(settlement.getOrder().getId().toString())
                        .amount(settlement.getOrder().getAuction().getWinPrice().toString())
                        .charge(settlement.getCharge().toString())
                        .settlementAmount(settlement.getSettlementAmount().toString())
                        .settlementDate(LocalDate.now().toString())
                        .createdAt(settlement.getCreated_at().toString())
                        .bankAccount(settlement.getOrder().getAuction().getProduct().getSeller().getAccountNumber())
                        .build();

        // 정산 처리
        settlement.settleNow(settlement.getOrder().getAuction().getWinPrice());

        // 파일 작성
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // CSV 헤더
            writer.writeNext(new String[]{"ID", "Seller ID", "Order Id", "Amount", "Charge", "Settlement Amount", "Settlement Date", "Created At", "Bank Account"});
            // 파일에 데이터 입력
                writer.writeNext(new String[]{
                        String.valueOf(dto.getSettlementId()),
                        String.valueOf(dto.getSellerId()),
                        String.valueOf(dto.getOrderId()),
                        String.valueOf(dto.getAmount()),
                        String.valueOf(dto.getCharge()),
                        String.valueOf(dto.getSettlementAmount()),
                        String.valueOf(dto.getSettlementDate()),
                        String.valueOf(dto.getCreatedAt()),
                        String.valueOf(dto.getBankAccount())
                });
        } catch (IOException e) {
            throw new ClientException(ErrorCode.SETTLEMENT_SAVE_FAILED);
        }
    }
}
