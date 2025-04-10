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

    //User 에러 코드
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-1", "해당 유저를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER-2", "비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED_PROFILE_UPDATE(HttpStatus.UNAUTHORIZED, "USER-3", "해당 유저에 대한 프로필 수정 권한이 없습니다."),
    UNAUTHORIZED_PASSWORD_UPDATE(HttpStatus.UNAUTHORIZED, "USER-4", "해당 유저에 대한 비밀번호 수정 권한이 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "USER-5", "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER-6", "이미 존재하는 이메일입니다."),
    INVALID_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "USER-7", "기존의 비밀번호가 아닙니다."),
    PASSWORD_NOT_CHANGED(HttpStatus.BAD_REQUEST, "USER-8", "기존의 비밀번호와 다르게 입력해 주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "USER-9", "잘못된 토큰입니다."),
    ALREADY_LOGOUT(HttpStatus.BAD_REQUEST, "USER-10", "이미 로그아웃 되었습니다."),

    // Auction Exceptions
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "AUCTION-1", "경매가 존재하지 않습니다."),

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
    
  
    // Uncaught Exceptions
    EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "EXCEPTION", "알 수 없는 에러입니다.");
  
    private final HttpStatus status;
    private final String code;
    private final String message;
}
