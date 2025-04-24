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
    private String title; // 상품명
    private Long lprice; // 최저가
    private String mallName; // 쇼핑몰 이름
    private String link; // 카탈로그 상품 판별
    private String productId; // 카탈로그 상품 판별
}
