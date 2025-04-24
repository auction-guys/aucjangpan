package com.fifteen.auction.global.client.naver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NaverShoppingSearchResponse {
    private List<NaverShoppingItemDto> items;
}