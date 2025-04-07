package com.fifteen.auction.global.dto.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * 여기에 에러 코드를 추가하세요
     * '// user 에러 코드' 와 같은 주석으로 도메인을 분리해주시면 됩니다.
     * 상수를 선언하실 때, code 부분은 '도메인-숫자'의 형식으로 해주세요. 예) USER-1
     */
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-1", "해당 주문을 찾을 수 없습니다."),
    ORDER_NOT_MACHED(HttpStatus.BAD_REQUEST, "PAYMENT-1", "주문정보가 일치하지 않습니다."),

    // Auction Exceptions
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "AUCTION-1", "경매가 존재하지 않습니다."),

    // Uncaught Exceptions
    EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "EXCEPTION", "알 수 없는 에러입니다."),

    // Product Custom Exception
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-1", "존재하지 않는 상품입니다."),
    PRODUCT_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-2", "존재하지 않는 카테고리입니다."),
    PRODUCT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-3", "대표 이미지가 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-1", "존재하지 않는 사용자입니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "COMMON-1", "접근 권한이 없습니다."),

    // Favorite Exception
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAVORITE-1", "찜 내역이 존재하지 않습니다."),
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "AUCTION-1", "해당 경매가 존재하지 않습니다."),

    // S3 Exception
    S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3-1", "이미지 업로드에 실패했습니다."),
    S3_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3-2", "이미지 삭제에 실패했습니다."),
    S3_KEY_EXTRACTION_FAIL(HttpStatus.BAD_REQUEST, "S3-3", "키 추출에 실패했습니다."),
    S3_INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "S3-4", "파일 확장자를 찾을 수 없습니다."),
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
