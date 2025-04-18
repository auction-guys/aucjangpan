package com.fifteen.auction.global.client.naver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverShoppingItemDto {
    private String title;       // 상품명
    private Long lprice;        // 최저가
    private String mallName;    // 쇼핑몰 이름
}
