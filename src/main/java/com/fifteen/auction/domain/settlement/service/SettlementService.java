package com.fifteen.auction.domain.settlement.service;

import com.fifteen.auction.domain.settlement.dto.response.SettlementResponse;
import com.fifteen.auction.domain.settlement.entity.Settlement;
import com.fifteen.auction.domain.settlement.enums.SettlementStatus;
import com.fifteen.auction.domain.settlement.repository.SettlementRepository;
import com.fifteen.auction.global.dto.PageCond;
import com.fifteen.auction.global.dto.PageInfo;
import com.fifteen.auction.global.dto.Response;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import com.fifteen.auction.global.dto.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final CsvUploadService csvUploadService;
    private final ChargeService chargeService;

    //TODO: 스케줄러 등록은 crud 끝나고나 고도화 때
    @Transactional
    public String settle() {

        // 수정 findAll
        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.PENDING);

        // 정산 가능 데이터 존재 여부
        if (settlements.isEmpty()) {
            throw new ServerException(ErrorCode.SETTLEMENT_NOT_FOUND);
        }

        // 정산 처리
        for (Settlement s : settlements) {
            s.settled();
        }

        // csv 파일에 받은 데이터 정리
        List<SettlementResponse> list = settlements.stream()
                .map(SettlementResponse::from).toList();

        // TODO: 비동기화
        // 구글 스프레드 시트?
        // 파일 작성 후 S3에 저장 url 반환

        return csvUploadService.writeToCsv(list);
    }

    @Transactional
    public String settleImmediately(Long settlementId, Long currentUserId) {
        // 검증
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ClientException(ErrorCode.SETTLEMENT_NOT_FOUND));

        // csv 파일에 받은 데이터 정리
        SettlementResponse dto = SettlementResponse.from(settlement);

        // 정산 처리
        settlement.settleNow(currentUserId, chargeService.getImmediatelyCharge());

        // TODO: 비동기화
        // 파일 작성 후 S3에 저장 url 반환
        return csvUploadService.writeToCsv(dto);
    }

    @Transactional(readOnly = true)
    public Response<Page<SettlementResponse>> findSettlements(Long currentUserId, PageCond pageCond) {

        Pageable pageable = PageRequest.of(pageCond.getPageNum() - 1, pageCond.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SettlementResponse> pages = settlementRepository.findBySellerId(currentUserId, pageable);

        PageInfo pageInfo = PageInfo.builder()
                .pageNum(pageCond.getPageNum())
                .pageSize(pageCond.getPageSize())
                .totalPage(pages.getTotalPages())
                .totalElement(pages.getTotalElements())
                .build();

        return Response.of(pages, pageInfo);
    }


    @Transactional(readOnly = true)
    public SettlementResponse findSettlement(Long currentUserId, Long settlementId) {

        // 검증
        Settlement settlement = settlementRepository.findByIdSettlementId(settlementId)
                .orElseThrow(() -> new ClientException(ErrorCode.SETTLEMENT_NOT_FOUND));
        settlement.validateOwner(currentUserId);

        return SettlementResponse.from(settlement);
    }

    //TODO: 올린 파일 삭제가 필요할까?
}
